package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.MathHelper;

public class PitchFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Fly speed", 0.6, 0.1, 3.0));

    public PitchFly() {
        super("PitchFly", "Fly by looking up or down, horizontal based on yaw", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float pitch = mc.player.getPitch();
        float yaw = mc.player.getYaw();
        double s = speed.get();

        float yawRad = yaw * 0.017453292f;
        float pitchRad = pitch * 0.017453292f;

        double vy = 0;
        if (pitch < -30) {
            vy = s * (-pitch / 90.0);
        } else if (pitch > 30) {
            vy = -s * (pitch / 90.0);
        }

        double mx = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad) * s;
        double mz = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad) * s;

        mc.player.setVelocity(mx, vy, mz);
        mc.player.fallDistance = 0;
    }
}
