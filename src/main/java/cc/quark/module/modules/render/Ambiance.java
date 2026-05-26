package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class Ambiance extends Module {

    private final DoubleSetting gamma = register(new DoubleSetting(
            "Gamma", "Game gamma (1.0 = default; 10.0 = max brightness)", 1.0, 0.0, 15.0));
    private final BoolSetting applyGamma = register(new BoolSetting(
            "Apply Gamma", "Override the game's gamma setting", false));

    private double savedGamma = 1.0;

    public Ambiance() {
        super("Ambiance", "Fine-tune gamma and atmosphere settings", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) savedGamma = mc.options.getGamma().getValue();
    }

    @Override
    public void onDisable() {
        if (mc.options != null && applyGamma.isEnabled()) {
            mc.options.getGamma().setValue(savedGamma);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.options == null) return;
        if (applyGamma.isEnabled()) mc.options.getGamma().setValue(gamma.get());
    }
}
