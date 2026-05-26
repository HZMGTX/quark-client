package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * LedgeGrab - boosts the player up when a ledge is at head height ahead.
 */
public class LedgeGrab extends Module {

    public LedgeGrab() {
        super("LedgeGrab", "Pulls up onto ledges", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.input.movementForward <= 0 || mc.player.isOnGround()) return;
        Direction facing = mc.player.getHorizontalFacing();
        BlockPos front = mc.player.getBlockPos().offset(facing);
        if (!mc.world.getBlockState(front).isAir()
                && mc.world.getBlockState(front.up()).isAir()) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, 0.42, v.z);
        }
    }
}
