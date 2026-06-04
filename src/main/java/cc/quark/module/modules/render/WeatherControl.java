package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;

/**
 * WeatherControl - overrides client-side rain/thunder rendering.
 * Actual sky rendering is modified by accessing world rain level.
 */
public class WeatherControl extends Module {

    private static WeatherControl instance;

    private final ModeSetting weather = register(new ModeSetting(
            "Weather", "Override current weather display", "Clear",
            "Clear", "Rain", "Thunder"));

    public WeatherControl() {
        super("WeatherControl", "Client-side weather override", Category.RENDER);
        instance = this;
    }

    public static WeatherControl getInstance() { return instance; }

    public static String getWeatherMode() {
        if (instance == null || !instance.isEnabled()) return null;
        return instance.weather.get();
    }

    @Override
    public void onDisable() {
        // Weather reverts to server-side on disable
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null) return;

        // Override client-side rain gradient and thunder
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
