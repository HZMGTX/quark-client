package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoBridge3 — enhanced bridge-building with diagonal support.
 * Automatically places blocks beneath the player while bridging,
 * including diagonal directions when the player moves at an angle.
 */
public class AutoBridge3 extends Module {

    private final BoolSetting diagonal = register(new BoolSetting(
            "Diagonal", "Place blocks in diagonal directions as well", true));
    private final BoolSetting sneakRequired = register(new BoolSetting(
            "Sneak Required", "Only bridge while sneaking", true));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between block placements", 50, 0, 500));
    private final BoolSetting autoSneak = register(new BoolSetting(
            "Auto Sneak", "Automatically sneak at edge to avoid falling", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoBridge3() {
        super("AutoBridge3", "Enhanced bridge-building with diagonal support", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof BlockItem bi) {
                var block = bi.getBlock();
                if (block != Blocks.AIR && block != Blocks.TORCH &&
                        block != Blocks.WALL_TORCH && block != Blocks.SAND &&
                        block != Blocks.GRAVEL) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void tryPlace(BlockPos targetBelow, int blockSlot) {
        if (mc.world == null || mc.interactionManager == null) return;
        if (!mc.world.getBlockState(targetBelow).isAir()) return;

        // Need a solid neighbor to place against
        Direction[] faces = Direction.values();
        for (Direction face : faces) {
            BlockPos neighbor = targetBelow.offset(face);
            if (mc.world.getBlockState(neighbor).isSolidBlock(mc.world, neighbor)) {
                int saved = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = blockSlot;

                Vec3d hitVec = Vec3d.ofCenter(neighbor).add(
                        face.getOpposite().getOffsetX() * 0.5,
                        face.getOpposite().getOffsetY() * 0.5,
                        face.getOpposite().getOffsetZ() * 0.5);
                BlockHitResult hit = new BlockHitResult(hitVec, face.getOpposite(), neighbor, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);

                mc.player.getInventory().selectedSlot = saved;
                return;
            }
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (sneakRequired.isEnabled() && !mc.options.sneakKey.isPressed()) return;
        if (!timer.hasReached(delay.get())) return;

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        if (!moving) return;

        if (autoSneak.isEnabled()) {
            mc.options.sneakKey.setPressed(true);
        }

        // Always try directly below player
        BlockPos below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).isAir()) {
            tryPlace(below, blockSlot);
            timer.reset();
            return;
        }

        if (diagonal.isEnabled()) {
            // Determine player movement direction in world space
            float yaw = mc.player.getYaw();
            float fwd = mc.player.input.movementForward;
            float side = mc.player.input.movementSideways;
            double yawRad = Math.toRadians(yaw);

            double dx = -Math.sin(yawRad) * fwd + Math.cos(yawRad) * side;
            double dz =  Math.cos(yawRad) * fwd + Math.sin(yawRad) * side;

            int stepX = (int) Math.signum(dx);
            int stepZ = (int) Math.signum(dz);

            if (stepX != 0 && stepZ != 0) {
                // Diagonal bridging
                BlockPos diagBelow = mc.player.getBlockPos().add(stepX, -1, stepZ);
                if (mc.world.getBlockState(diagBelow).isAir()) {
                    tryPlace(diagBelow, blockSlot);
                    timer.reset();
                }
            }
        }
    }
}
