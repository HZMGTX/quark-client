package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class EntitySpeed extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal speed while riding an entity", 0.6, 0.1, 2.0));

    public EntitySpeed() {
        super("EntitySpeed", "Boosts movement speed while riding horses, boats, or other entities", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        Vec3d vel = vehicle.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hSpeed < speed.get()) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            double dx = -Math.sin(yaw) * speed.get();
            double dz =  Math.cos(yaw) * speed.get();
            vehicle.setVelocity(dx, vel.y, dz);
        }
    }
}
