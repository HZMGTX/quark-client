package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

/**
 * LavaSpeed - while in lava: apply Speed + Fire Resistance status effects and
 * apply a raw velocity multiplier each tick. SpeedMultiplier setting controls
 * how strong the boost is.
 */
public class LavaSpeed extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Lava speed multiplier", 1.5, 1.0, 4.0));

    public LavaSpeed() {
        super("LavaSpeed", "Faster lava travel with Fire Resistance and speed boost", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.SPEED);
        mc.player.removeStatusEffect(StatusEffects.FIRE_RESISTANCE);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isInLava()) return;

        // Apply status effects for visual/server-side speed and fire immunity
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, false, false));
        mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, 1, false, false));

        // Raw velocity boost along current horizontal direction
        Vec3d vel = mc.player.getVelocity();
        double hLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hLen > 0.001) {
            double factor = multiplier.get();
            mc.player.setVelocity(vel.x * factor, vel.y, vel.z * factor);
        }
    }
}
