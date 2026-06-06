package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * MountSpeed - applies a speed boost to any entity the player is riding.
 *
 * Works on horses, pigs, striders, boats, and any other rideable entity by
 * adding velocity in the vehicle's current horizontal travel direction each tick.
 */
public class MountSpeed extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Extra velocity added per tick to the mounted entity", 0.2, 0.01, 2.0));
    private final DoubleSetting minInputThreshold = register(new DoubleSetting(
            "Input Threshold", "Only boost when input magnitude exceeds this value", 0.05, 0.0, 0.5));

    public MountSpeed() {
        super("MountSpeed", "Speed boost for all mounted entities", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.hasVehicle()) return;

        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double inputMag = Math.sqrt(fwd * fwd + side * side);

        if (inputMag < minInputThreshold.get()) return;

        Vec3d vel = vehicle.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        if (hSpeed < 0.01) {
            // No current horizontal velocity — boost in look direction
            float yawRad = (float) Math.toRadians(mc.player.getYaw());
            double addX = -Math.sin(yawRad) * speed.get();
            double addZ =  Math.cos(yawRad) * speed.get();
            vehicle.setVelocity(vel.x + addX, vel.y, vel.z + addZ);
        } else {
            // Scale existing velocity
            double factor = 1.0 + speed.get() / Math.max(hSpeed, 0.01);
            vehicle.setVelocity(vel.x * factor, vel.y, vel.z * factor);
        }
    }
}
