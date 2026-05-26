package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * AirStrafe - lets the player steer horizontal velocity while airborne.
 */
public class AirStrafe extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Air strafe speed", 0.28, 0.1, 0.6));

    public AirStrafe() {
        super("AirStrafe", "Steer while airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround() || mc.player.input.movementForward == 0) return;
        float yaw = mc.player.getYaw();
        double mx = -MathHelper.sin(yaw * 0.017453292f) * speed.get();
        double mz = MathHelper.cos(yaw * 0.017453292f) * speed.get();
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(mx, v.y, mz);
    }
}
