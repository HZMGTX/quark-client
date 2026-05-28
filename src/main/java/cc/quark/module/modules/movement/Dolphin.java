package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.Vec3d;

public class Dolphin extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Dolphin boost speed", 1.5, 0.5, 4.0));

    private final TimerUtil timer = new TimerUtil();

    public Dolphin() {
        super("Dolphin", "Auto-dolphin forward in water", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;
        if (!mc.options.jumpKey.isPressed()) return;

        double intervalMs = Math.max(100, 600 / speed.get());

        if (timer.hasReached(intervalMs)) {
            timer.reset();
            Vec3d vel = mc.player.getVelocity();
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            float pitch = (float) Math.toRadians(mc.player.getPitch());

            double hSpeed = speed.get() * 0.25;
            double vSpeed = -Math.sin(pitch) * hSpeed;
            double fwdX = -Math.sin(yaw) * Math.cos(pitch) * hSpeed;
            double fwdZ = Math.cos(yaw) * Math.cos(pitch) * hSpeed;

            mc.player.setVelocity(
                    vel.x + fwdX,
                    vel.y + vSpeed + 0.1,
                    vel.z + fwdZ
            );
        }
    }
}
