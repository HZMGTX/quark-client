package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

/**
 * BoatSpeed - when the player is riding a boat, adds velocity in the direction
 * the boat is facing each tick.
 */
public class BoatSpeed extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Extra velocity added per tick in the boat's facing direction", 0.15, 0.05, 0.5));

    public BoatSpeed() {
        super("BoatSpeed", "Adds extra speed to boats in their facing direction", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!(mc.player.getVehicle() instanceof BoatEntity boat)) return;

        float yawRad = (float) Math.toRadians(boat.getYaw());
        double spd = speed.get();

        double addX = -Math.sin(yawRad) * spd;
        double addZ = Math.cos(yawRad) * spd;

        Vec3d vel = boat.getVelocity();
        boat.setVelocity(vel.x + addX, vel.y, vel.z + addZ);
    }
}
