package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class AFKMode extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "AFK action to perform",
            "Rotate", "Rotate", "Walk", "Swing", "Jump"));
    private final IntSetting interval = register(new IntSetting(
            "Interval", "Seconds between actions", 30, 1, 120));
    private final DoubleSetting rotateSpeed = register(new DoubleSetting(
            "RotateSpeed", "Degrees per action (Rotate mode)", 5.0, 1.0, 30.0));

    private final TimerUtil timer = new TimerUtil();
    private final Random rand = new Random();
    private float yawDir = 1f;

    public AFKMode() {
        super("AFKMode", "Prevents AFK kick using periodic actions", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(interval.get() * 1000L)) return;
        timer.reset();

        switch (mode.get()) {
            case "Rotate" -> {
                // Alternate direction each time with small random offset
                yawDir = -yawDir;
                float degrees = (float) rotateSpeed.get() + (rand.nextFloat() * 3f - 1.5f);
                mc.player.setYaw(mc.player.getYaw() + degrees * yawDir);
            }
            case "Walk" -> {
                // Nudge forward then back using velocity
                Vec3d vel = mc.player.getVelocity();
                float yaw = (float) Math.toRadians(mc.player.getYaw());
                double dx = -Math.sin(yaw) * 0.15;
                double dz = Math.cos(yaw) * 0.15;
                mc.player.setVelocity(vel.x + dx, vel.y, vel.z + dz);
            }
            case "Swing" -> {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            case "Jump" -> {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
        }
    }
}
