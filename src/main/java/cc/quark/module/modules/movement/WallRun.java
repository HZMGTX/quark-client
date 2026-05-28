package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class WallRun extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Wall run speed", 0.3, 0.1, 1.0));

    public WallRun() {
        super("WallRun", "Run along walls horizontally when pressed against one", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isOnGround()) return;

        BlockPos playerPos = mc.player.getBlockPos();
        Direction wallFacing = null;

        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos adjacent = playerPos.offset(dir);
            if (!mc.world.getBlockState(adjacent).isAir()) {
                wallFacing = dir;
                break;
            }
        }

        if (wallFacing == null) return;

        Direction parallel = wallFacing.rotateYClockwise();
        double s = speed.get();
        double vx = parallel.getOffsetX() * s;
        double vz = parallel.getOffsetZ() * s;

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vx, 0.0, vz);
        mc.player.fallDistance = 0;
    }
}
