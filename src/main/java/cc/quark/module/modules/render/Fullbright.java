package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class Fullbright extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Fullbright implementation method", "Gamma", "Gamma", "Night Vision", "Both"));

    private final DoubleSetting gammaValue = register(new DoubleSetting(
            "Gamma Value", "Gamma level to apply (vanilla max is ~1.0, higher = brighter)", 16.0, 1.0, 1000.0));

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
        if (mc.options != null && (mode.is("Gamma") || mode.is("Both"))) {
            mc.options.getGamma().setValue(savedGamma);
        }
        if (mc.player != null && (mode.is("Night Vision") || mode.is("Both"))) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.options == null) return;

        if (mode.is("Gamma") || mode.is("Both")) {
            mc.options.getGamma().setValue(gammaValue.get());
        }

        if (mode.is("Night Vision") || mode.is("Both")) {
            StatusEffectInstance current = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (current == null || current.getDuration() < 200) {
                mc.player.addStatusEffect(
                        new StatusEffectInstance(StatusEffects.NIGHT_VISION, 9999, 0, false, false, false));
            }
        }
    }
}
