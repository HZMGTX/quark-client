package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/**
 * Fullbright - makes the game render as if it were fully lit everywhere.
 *
 * Gamma mode:       Sets the game's gamma option to 10.0 (vanilla gamma hack).
 * Night Vision mode: Applies infinite night vision potion effect.
 */
public class Fullbright extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Fullbright implementation method", "Gamma", "Gamma", "Night Vision"));

    private double savedGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Removes darkness from the world for full visibility", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            savedGamma = mc.options.getGamma().getValue();
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null && mode.is("Gamma")) {
            mc.options.getGamma().setValue(savedGamma);
        }
        if (mc.player != null && mode.is("Night Vision")) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.options == null) return;

        if (mode.is("Gamma")) {
            // Force gamma to 10.0 every tick so it doesn't get reset by slider
            mc.options.getGamma().setValue(10.0);
        } else if (mode.is("Night Vision")) {
            // Apply night vision with a large duration so it doesn't expire
            // Re-apply every 20 ticks to keep duration fresh
            StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (current == null || current.getDuration() < 200) {
                mc.player.addStatusEffect(
                        new StatusEffectInstance(StatusEffects.NIGHT_VISION, 9999, 0, false, false, false));
            }
        }
    }
}
