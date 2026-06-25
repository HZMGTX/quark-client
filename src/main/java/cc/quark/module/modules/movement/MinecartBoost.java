package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;

public class MinecartBoost extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting("Multiplier", "Speed multiplier for minecart", 2.0, 1.0, 10.0));

    public MinecartBoost() {
        super("MinecartBoost", "Activates powered rails and boosts minecart speed", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!(mc.player.getVehicle() instanceof AbstractMinecartEntity cart)) return;

        Vec3d vel = cart.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hSpeed < 0.001) return;

        double factor = multiplier.get();
        cart.setVelocity(vel.x * factor, vel.y, vel.z * factor);
        cart.velocityModified = true;
    }
}
