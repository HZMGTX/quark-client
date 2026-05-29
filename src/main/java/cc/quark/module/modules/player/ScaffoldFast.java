package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;

public class ScaffoldFast extends Module {

    private final BoolSetting onlyBackward = register(new BoolSetting(
            "Only Backward", "Only scaffold when moving backward", true));
    private final BoolSetting safeWalk = register(new BoolSetting(
            "Safe Walk", "Slow down at block edges to prevent falling", true));

    private Field blockPlaceCooldownField = null;

    public ScaffoldFast() {
        super("ScaffoldFast", "Places blocks beneath feet at 1 block/tick while bridging backward", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Require holding a block
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        // Direction restriction
        if (onlyBackward.isEnabled() && mc.player.input.movementForward >= 0) return;

        // Zero block placement cooldown via reflection
        zeroCooldown();

        // Safe-walk: reduce speed at edges
        if (safeWalk.isEnabled()) {
            BlockPos below = mc.player.getBlockPos().down();
            if (mc.world.getBlockState(below).isAir()) {
                // Slow down
                Vec3d vel = mc.player.getVelocity();
                mc.player.setVelocity(vel.x * 0.6, vel.y, vel.z * 0.6);
            }
        }

        // Try to place block directly below the player
        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) return;

        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos adj = below.offset(dir);
            if (mc.world.getBlockState(adj).isAir()) continue;

            Vec3d hitVec = Vec3d.ofCenter(adj).add(
                    dir.getOpposite().getOffsetX() * 0.5,
                    dir.getOpposite().getOffsetY() * 0.5,
                    dir.getOpposite().getOffsetZ() * 0.5);

            BlockHitResult hitResult = new BlockHitResult(hitVec, dir.getOpposite(), adj, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            break;
        }
    }

    private void zeroCooldown() {
        try {
            if (blockPlaceCooldownField == null) {
                blockPlaceCooldownField = findField(mc.interactionManager.getClass(),
                        "blockBreakingCooldown", "field_2692");
            }
            if (blockPlaceCooldownField != null) {
                blockPlaceCooldownField.setAccessible(true);
                blockPlaceCooldownField.set(mc.interactionManager, 0);
            }
        } catch (Exception ignored) {}
    }

    private static Field findField(Class<?> c, String... names) {
        for (String name : names) {
            Class<?> cl = c;
            while (cl != null) {
                try { return cl.getDeclaredField(name); } catch (NoSuchFieldException e) {}
                cl = cl.getSuperclass();
            }
        }
        return null;
    }
}
