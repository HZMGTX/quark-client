package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;

public class WeatherChanger extends Module {

    private final ModeSetting weather = register(new ModeSetting(
            "Mode", "Override weather visuals", "Clear", "Clear", "Rain", "Thunder", "Default"));

    public WeatherChanger() {
        super("WeatherChanger", "Overrides weather rendering — always clear sky, rain, or thunder", Category.RENDER);
    }

    /** Cancel incoming weather-change packets that would contradict our desired state. */
    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (weather.is("Default")) return;
        if (!(event.getPacket() instanceof GameStateChangeS2CPacket pkt)) return;

        GameStateChangeS2CPacket.Reason reason = pkt.getReason();

        if (weather.is("Clear")) {
            // Block any packet that would start or strengthen rain/thunder
            if (reason == GameStateChangeS2CPacket.RAIN_STARTED
                    || reason == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED
                    || reason == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
                event.cancel();
            }
        } else if (weather.is("Rain")) {
            // Block packets that would clear the sky or send thunder gradients away from 0
            if (reason == GameStateChangeS2CPacket.RAIN_STOPPED
                    || reason == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED) {
                event.cancel();
            }
        } else if (weather.is("Thunder")) {
            // Block packets that would clear rain or stop thunder
            if (reason == GameStateChangeS2CPacket.RAIN_STOPPED) {
                event.cancel();
            }
        }
    }

    /** Every tick, forcibly apply rain/thunder strength to client world so it stays consistent. */
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || weather.is("Default")) return;

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

    @Override
    public String getSuffix() {
        return weather.get();
    }
}
