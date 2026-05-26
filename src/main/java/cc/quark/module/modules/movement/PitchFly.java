package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * PitchFly - flight that follows both yaw and pitch of the camera.
 */
public class PitchFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Fly speed", 0.6, 0.1, 2.0));

    public PitchFly() {
        super("PitchFly", "Flight following camera pitch", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.input.movementForward == 0) {
            mc.player.setVelocity(0, 0, 0);
            return;
        }
        float yaw = mc.player.getYaw() * 0.017453292f;
        float pitch = mc.player.getPitch() * 0.017453292f;
        double mx = -MathHelper.sin(yaw) * MathHelper.cos(pitch) * speed.get();
        double my = -MathHelper.sin(pitch) * speed.get();
        double mz = MathHelper.cos(yaw) * MathHelper.cos(pitch) * speed.get();
        Vec3d ignored = mc.player.getVelocity();
        mc.player.setVelocity(mx, my, mz);
    }
}
