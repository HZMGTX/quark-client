package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AntiTrap - detects when all four horizontal sides are blocked by solid blocks
 * and attempts to escape by auto-jumping or auto-crouching.
 *
 * <ul>
 *   <li><b>Jump</b>  - apply upward velocity to hop out the top.</li>
 *   <li><b>Crouch</b>- hold sneak position to stay low and avoid ceiling trap.</li>
 *   <li><b>Both</b>  - try jumping first; if ceiling blocks it, crouch.</li>
 * </ul>
 */
public class AntiTrap extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Escape strategy when trapped", "Jump", "Jump", "Crouch", "Both"));
    private final BoolSetting onlySurrounded = register(new BoolSetting(
            "Only Surrounded", "Only activate when all 4 horizontal sides are blocked", true));

    public AntiTrap() {
        super("AntiTrap", "Auto-escape from block traps", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos pos = mc.player.getBlockPos();

        boolean trapped;
        if (onlySurrounded.isEnabled()) {
            // Check all four horizontal directions
            int blocked = 0;
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos neighbour = pos.offset(dir);
                if (!mc.world.getBlockState(neighbour).isAir()) blocked++;
            }
            trapped = (blocked >= 4);
        } else {
            // Trigger if any side is blocked
            int blocked = 0;
            for (Direction dir : Direction.Type.HORIZONTAL) {
                if (!mc.world.getBlockState(pos.offset(dir)).isAir()) blocked++;
            }
            trapped = (blocked >= 1);
        }

        if (!trapped) return;

        boolean ceilingBlocked = !mc.world.getBlockState(pos.up()).isAir()
                || !mc.world.getBlockState(pos.up().up()).isAir();

        Vec3d vel = mc.player.getVelocity();

        switch (mode.get()) {
            case "Jump" -> mc.player.setVelocity(vel.x, 0.42, vel.z);
            case "Crouch" -> mc.player.input.sneaking = true;
            case "Both" -> {
                if (!ceilingBlocked) {
                    mc.player.setVelocity(vel.x, 0.42, vel.z);
                } else {
                    mc.player.input.sneaking = true;
                }
            }
        }
    }
}
