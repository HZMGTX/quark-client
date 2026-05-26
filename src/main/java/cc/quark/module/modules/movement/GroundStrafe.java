package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * GroundStrafe - steers ground velocity toward the look direction.
 */
public class GroundStrafe extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Strafe speed", 0.3, 0.1, 0.8));

    public GroundStrafe() {
        super("GroundStrafe", "Look-aligned ground strafe", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround() || mc.player.input.movementForward == 0) return;
        float yaw = mc.player.getYaw();
        double mx = -MathHelper.sin(yaw * 0.017453292f) * speed.get();
        double mz = MathHelper.cos(yaw * 0.017453292f) * speed.get();
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(mx, v.y, mz);
    }
}
