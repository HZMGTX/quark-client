package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

public class PingAlert extends Module {
    private final IntSetting threshold = register(new IntSetting("Threshold", "Ping threshold in ms", 200, 50, 2000));
    private final TimerUtil timer = new TimerUtil();

    public PingAlert() {
        super("Ping Alert", "Alerts when ping exceeds threshold", Category.MISC, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!timer.hasReached(5000)) return;
        var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry == null) return;
        int ping = entry.getLatency();
        if (ping > threshold.get()) {
            ChatUtil.warn("[PingAlert] High ping: " + ping + "ms");
            timer.reset();
        }
    }
}
