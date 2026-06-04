package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;

public class WeatherMod extends Module {

    private final ModeSetting weather = register(new ModeSetting(
            "Weather", "Client-side weather to display", "Clear", "Clear", "Rain", "Thunder"));

    public WeatherMod() {
        super("WeatherMod", "Client-side weather control", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null) return;

        switch (weather.get()) {
            case "Clear" -> {
                mc.world.setRainGradient(0f);
                mc.world.setThunderGradient(0f);
            }
            case "Rain" -> {
                mc.world.setRainGradient(1f);
                mc.world.setThunderGradient(0f);
            }
            case "Thunder" -> {
                mc.world.setRainGradient(1f);
                mc.world.setThunderGradient(1f);
            }
        }
    }
}
