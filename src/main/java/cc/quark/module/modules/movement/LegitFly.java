package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * LegitFly - a slower, less suspicious flight implementation.
 * Applies small upward velocity when jump is held, small downward when sneak
 * is held, and zeroes Y velocity otherwise to maintain altitude.
 */
public class LegitFly extends Module {

    private final DoubleSetting riseSpeed = register(new DoubleSetting(
            "RiseSpeed", "Upward velocity when jump key is held", 0.08, 0.01, 0.5));
    private final DoubleSetting fallSpeed = register(new DoubleSetting(
            "FallSpeed", "Downward velocity when sneak key is held", 0.04, 0.01, 0.3));

    public LegitFly() {
        super("LegitFly", "Slow, less suspicious fly using jump/sneak keys for vertical control", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.player.fallDistance = 0;
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.fallDistance = 0;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        Vec3d vel = mc.player.getVelocity();
        double vy;

        if (mc.options.jumpKey.isPressed()) {
            vy = riseSpeed.get();
        } else if (mc.options.sneakKey.isPressed()) {
            vy = -fallSpeed.get();
        } else {
            vy = 0.0;
        }

        mc.player.setVelocity(vel.x, vy, vel.z);
        mc.player.fallDistance = 0;
    }
}
