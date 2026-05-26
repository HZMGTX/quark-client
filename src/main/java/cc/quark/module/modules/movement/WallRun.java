package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * WallRun - holds the player's height while sprinting alongside a wall.
 */
public class WallRun extends Module {

    public WallRun() {
        super("WallRun", "Run along walls", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isSprinting() || mc.player.isOnGround()) return;
        Direction facing = mc.player.getHorizontalFacing();
        BlockPos side = mc.player.getBlockPos().offset(facing.rotateYClockwise());
        if (mc.world.getBlockState(side).isAir()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, 0.0, v.z);
    }
}
