package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
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

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (weather.is("Default")) return;
        if (!(event.getPacket() instanceof GameStateChangeS2CPacket pkt)) return;

        GameStateChangeS2CPacket.Reason reason = pkt.getReason();

        if (weather.is("Clear") && (reason == GameStateChangeS2CPacket.BEGIN_RAINING
                || reason == GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED
                || reason == GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED)) {
            event.cancel();
        } else if (weather.is("Rain") && reason == GameStateChangeS2CPacket.STOP_RAINING) {
            event.cancel();
        } else if (weather.is("Thunder") && reason == GameStateChangeS2CPacket.STOP_RAINING) {
            event.cancel();
        }
    }
}
