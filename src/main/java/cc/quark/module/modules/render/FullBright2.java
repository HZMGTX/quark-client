package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class FullBright2 extends Module {

    private final DoubleSetting level = register(new DoubleSetting("Gamma", "Gamma/brightness level", 10.0, 1.0, 20.0));

    private double savedGamma = 1.0;

    public FullBright2() {
        super("FullBright2", "Enhanced fullbright with configurable gamma level", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) savedGamma = mc.options.getGamma().getValue();
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.getGamma().setValue(savedGamma);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.options != null) mc.options.getGamma().setValue(level.get());
    }
}
