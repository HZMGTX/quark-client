package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * NightVision — applies a permanent night vision status effect locally on the
 * client so the player can always see clearly without consuming potions.
 * The effect is removed when the module is disabled.
 */
public class NightVision extends Module {

    private final BoolSetting hideParticles = register(new BoolSetting(
            "Hide Particles", "Suppress the effect's particle display", true));

    // Duration to refresh to (keep high to avoid flicker near expiry)
    private static final int REFRESH_TICKS = 400;
    private static final int REFRESH_WHEN_BELOW = 60;

    public NightVision() {
        super("NightVision", "Applies permanent night vision effect", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        applyEffect();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        StatusEffectInstance existing = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        if (existing == null || existing.getDuration() < REFRESH_WHEN_BELOW) {
            applyEffect();
        }
    }

    private void applyEffect() {
        if (mc.player == null) return;
        StatusEffectInstance effect = new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                REFRESH_TICKS,              // duration in ticks
                0,                          // amplifier (0 = level I)
                false,                      // ambient
                !hideParticles.isEnabled()  // show particles
        );
        mc.player.addStatusEffect(effect);
    }
}
