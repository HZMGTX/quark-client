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
 * Wallclimb - scales walls automatically while moving forward into them.
 */
public class Wallclimb extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Climb speed", 0.15, 0.05, 0.42));

    public Wallclimb() {
        super("Wallclimb", "Scale walls forward", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.input.movementForward <= 0) return;
        Direction facing = mc.player.getHorizontalFacing();
        BlockPos front = mc.player.getBlockPos().offset(facing);
        if (mc.world.getBlockState(front).isAir()) return;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x, speed.get(), v.z);
    }
}
