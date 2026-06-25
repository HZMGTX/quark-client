package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

public class BoatGlide extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Glide speed multiplier", 1.5, 0.5, 5.0));

    public BoatGlide() {
        super("BoatGlide", "Smoothly glides in a boat ignoring terrain friction", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!(mc.player.getVehicle() instanceof BoatEntity boat)) return;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        double fwd = mc.player.input.movementForward;
        double side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double len = Math.sqrt(fwd * fwd + side * side);
        double nx = fwd / len;
        double nz = side / len;

        double vx = (-Math.sin(yawRad) * nx + Math.cos(yawRad) * nz) * speed.get() * 0.2;
        double vz = ( Math.cos(yawRad) * nx + Math.sin(yawRad) * nz) * speed.get() * 0.2;

        Vec3d current = boat.getVelocity();
        boat.setVelocity(vx, current.y, vz);
        boat.velocityModified = true;
    }
}
