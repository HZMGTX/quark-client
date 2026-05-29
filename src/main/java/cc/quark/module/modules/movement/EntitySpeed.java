package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * EntitySpeed - boost the movement speed of any entity being ridden (horse,
 * minecart, boat, etc.) by overriding its velocity each tick.
 *
 * <p>Speed is applied in the player's look direction.  Optionally requires the
 * forward movement key to be held so the boost is only active while steering.
 */
public class EntitySpeed extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Target horizontal speed (blocks/tick) for the ridden entity", 0.6, 0.1, 3.0));
    private final BoolSetting requireForward = register(new BoolSetting(
            "Require Forward", "Only boost while pressing the forward movement key", false));
    private final BoolSetting multiplyMode = register(new BoolSetting(
            "Multiply Mode", "Multiply existing speed instead of setting it directly", false));
    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Speed multiplier when Multiply Mode is on", 1.5, 1.0, 5.0));

    public EntitySpeed() {
        super("EntitySpeed", "Boost horizontal speed while riding any entity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        if (requireForward.isEnabled() && mc.player.input.movementForward <= 0) return;

        Vec3d vel = vehicle.getVelocity();

        if (multiplyMode.isEnabled()) {
            vehicle.setVelocity(vel.x * multiplier.get(), vel.y, vel.z * multiplier.get());
        } else {
            double spd    = speed.get();
            double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

            if (hSpeed < spd) {
                double yawRad = Math.toRadians(mc.player.getYaw());
                float fwd  = mc.player.input.movementForward;
                float side = mc.player.input.movementSideways;

                if (fwd == 0 && side == 0) {
                    // No input — keep look direction
                    fwd = 1;
                }

                double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side);
                double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side);
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0) { dx /= len; dz /= len; }

                vehicle.setVelocity(dx * spd, vel.y, dz * spd);
            }
        }
    }
}
