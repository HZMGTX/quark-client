package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Momentum - stores last-ground horizontal velocity; while airborne with no
 * movement input, reapplies it each tick multiplied by Retention (0.8-1.0) so
 * the player glides on their last trajectory.
 */
public class Momentum extends Module {

    private final DoubleSetting retention = register(new DoubleSetting(
            "Retention", "Fraction of momentum kept per air tick", 0.95, 0.8, 1.0));

    private double storedX = 0;
    private double storedZ = 0;

    public Momentum() {
        super("Momentum", "Preserve last-ground horizontal velocity while airborne with no input", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        storedX = 0;
        storedZ = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (mc.player.isOnGround()) {
            Vec3d vel = mc.player.getVelocity();
            // Capture last ground horizontal velocity
            storedX = vel.x;
            storedZ = vel.z;
            return;
        }

        // Airborne with no movement input — reapply stored momentum
        if (fwd != 0 || side != 0) return;

        double horiz = Math.sqrt(storedX * storedX + storedZ * storedZ);
        if (horiz < 0.001) return;

        // Decay slightly each tick
        storedX *= retention.get();
        storedZ *= retention.get();

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(storedX, vel.y, storedZ);
    }
}
