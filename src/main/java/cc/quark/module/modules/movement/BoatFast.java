package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class BoatFast extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Boat movement speed multiplier", 2.0, 1.0, 10.0));

    public BoatFast() {
        super("BoatFast", "Speeds up boat movement significantly", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Check if player is riding a boat
        if (!(mc.player.getVehicle() instanceof BoatEntity boat)) return;

        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        float yaw = boat.getYaw();
        double yawRad = Math.toRadians(yaw);
        double len = Math.sqrt(fwd * fwd + side * side);
        double dx = (-Math.sin(yawRad) * (fwd / len) + Math.cos(yawRad) * (side / len));
        double dz = (Math.cos(yawRad) * (fwd / len) + Math.sin(yawRad) * (side / len));

        // Default boat speed is ~0.4 blocks/tick, multiply by configured speed
        double targetSpeed = 0.4 * speed.get();
        Vec3d vel = boat.getVelocity();
        boat.setVelocity(dx * targetSpeed, vel.y, dz * targetSpeed);
    }
}
