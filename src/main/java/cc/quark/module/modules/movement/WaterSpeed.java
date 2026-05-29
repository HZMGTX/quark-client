package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

/**
 * WaterSpeed - in water, apply Dolphins Grace status effect and a raw velocity
 * multiplier each tick. Speed setting scales the multiplier.
 */
public class WaterSpeed extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Water speed multiplier", 1.5, 1.0, 4.0));
    private final BoolSetting dolphinsGrace = register(new BoolSetting(
            "Dolphins Grace", "Apply Dolphins Grace status effect", true));
    private final BoolSetting surfaceJump = register(new BoolSetting(
            "Surface Jump", "Jump up when reaching water surface with Jump held", true));

    public WaterSpeed() {
        super("WaterSpeed", "Faster swimming with Dolphins Grace and velocity boost", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.DOLPHINS_GRACE);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;

        if (dolphinsGrace.isEnabled()) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.DOLPHINS_GRACE, 40, 0, false, false));
        }

        Vec3d vel = mc.player.getVelocity();

        // Surface jump
        if (surfaceJump.isEnabled() && !mc.player.isSubmergedInWater()
                && vel.y > 0 && mc.options.jumpKey.isPressed()) {
            mc.player.setVelocity(vel.x, 0.42, vel.z);
            return;
        }

        // Horizontal boost
        double hLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hLen > 0.001) {
            double factor = multiplier.get();
            mc.player.setVelocity(vel.x * factor, vel.y, vel.z * factor);
        }
    }
}
