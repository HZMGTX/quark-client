package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * WallClimb - allows the player to scale vertical walls by holding the jump key
 * while pressing against a solid surface.
 *
 * <p>Each tick where the player is horizontally adjacent to a solid block and is
 * pressing jump, an upward velocity impulse is applied to simulate climbing.
 * A {@code climbSpeed} setting controls how fast the player ascends.
 */
public class WallClimb extends Module {

    private final DoubleSetting climbSpeed = register(new DoubleSetting(
            "Climb Speed", "Upward velocity per tick while climbing (blocks/tick)", 0.2, 0.05, 1.0));

    private final BoolSetting requireJump = register(new BoolSetting(
            "Require Jump", "Only climb while the jump key is held", true));

    private final BoolSetting requireSprint = register(new BoolSetting(
            "Require Sprint", "Only climb while sprinting into the wall", false));

    private final BoolSetting noFallOnRelease = register(new BoolSetting(
            "No Fall", "Zero Y velocity when releasing jump mid-climb", false));

    public WallClimb() {
        super("WallClimb", "Allows climbing walls by holding jump against a surface", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isOnGround()) return;

        boolean jumpHeld = mc.options.jumpKey.isPressed();
        if (requireJump.isEnabled() && !jumpHeld) {
            if (noFallOnRelease.isEnabled() && isAgainstWall()) {
                Vec3d vel = mc.player.getVelocity();
                if (vel.y < 0) mc.player.setVelocity(vel.x, 0, vel.z);
            }
            return;
        }

        if (requireSprint.isEnabled() && !mc.player.isSprinting()) return;

        if (isAgainstWall()) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, climbSpeed.get(), vel.z);
        }
    }

    /**
     * Returns true if the player is horizontally adjacent to a solid block.
     */
    private boolean isAgainstWall() {
        BlockPos pos = mc.player.getBlockPos();
        // Check the four cardinal directions for a solid block at player body level
        int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] off : offsets) {
            BlockPos check = pos.add(off[0], 0, off[1]);
            if (!mc.world.getBlockState(check).isAir()
                    && mc.world.getBlockState(check).getBlock() != Blocks.WATER
                    && mc.world.getBlockState(check).getBlock() != Blocks.LAVA) {
                return true;
            }
            // Also check at eye level
            BlockPos checkEye = pos.add(off[0], 1, off[1]);
            if (!mc.world.getBlockState(checkEye).isAir()
                    && mc.world.getBlockState(checkEye).getBlock() != Blocks.WATER
                    && mc.world.getBlockState(checkEye).getBlock() != Blocks.LAVA) {
                return true;
            }
        }
        return false;
    }
}
