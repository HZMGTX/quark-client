package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * WallRun - detect a solid block on either side of the player while airborne
 * and pressing toward it; apply upward+forward velocity to run along the wall.
 */
public class WallRun extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Wall-run horizontal speed", 0.3, 0.05, 1.0));
    private final DoubleSetting upward = register(new DoubleSetting(
            "Upward", "Upward velocity while wall-running", 0.12, 0.0, 0.5));

    public WallRun() {
        super("WallRun", "Apply upward+forward velocity while pressing against a wall in air", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isOnGround()) return;

        // Only trigger when moving forward/sideways
        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        BlockPos playerPos = mc.player.getBlockPos();
        Direction wallDir  = null;

        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos adj = playerPos.offset(dir);
            if (!mc.world.getBlockState(adj).isAir()
             && !mc.world.getBlockState(adj.up()).isAir()) {
                wallDir = dir;
                break;
            }
        }

        if (wallDir == null) return;

        // Run along the wall (perpendicular direction at configured speed)
        Direction runDir = wallDir.rotateYClockwise();
        double s  = speed.get();
        double vx = runDir.getOffsetX() * s;
        double vz = runDir.getOffsetZ() * s;

        mc.player.setVelocity(vx, upward.get(), vz);
        mc.player.fallDistance = 0;
    }
}
