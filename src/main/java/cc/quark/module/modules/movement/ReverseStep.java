package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * ReverseStep - when the player walks off a ledge, instead of abruptly falling,
 * apply a controlled downward velocity to create a smooth step-down animation.
 * Resets fall distance to avoid damage on small drops.
 */
public class ReverseStep extends Module {

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Max block height for smooth step-down", 1.0, 0.5, 3.0));
    private final DoubleSetting stepSpeed = register(new DoubleSetting(
            "Step Speed", "Downward velocity applied during step-down", 0.4, 0.1, 1.0));

    public ReverseStep() {
        super("ReverseStep", "Smooth step-down off ledges instead of falling", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        Vec3d vel = mc.player.getVelocity();
        if (Math.abs(vel.x) < 0.01 && Math.abs(vel.z) < 0.01) return;

        // Check if the block below movement direction is air (ledge edge)
        double yawRad = Math.toRadians(mc.player.getYaw());
        double fwd = mc.player.input.movementForward;
        double side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        // Check directly below current position
        BlockPos below = mc.player.getBlockPos().down();
        boolean airBelow = mc.world.getBlockState(below).isAir();

        if (airBelow) {
            // Smooth step down by limiting downward speed
            double targetY = -Math.min(stepSpeed.get(), height.get() * 0.5);
            mc.player.setVelocity(vel.x, targetY, vel.z);
            mc.player.fallDistance = 0;
        }
    }
}
