package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConnectionMonitor extends Module {

    private final IntSetting highPingThreshold = register(new IntSetting(
            "High Ping Alert", "Alert when a player's ping exceeds this value (ms)", 500, 100, 5000));
    private final IntSetting updateInterval = register(new IntSetting(
            "Update Interval", "How often to check pings (ticks)", 40, 10, 200));
    private final BoolSetting showAll = register(new BoolSetting(
            "Show All", "Print all player pings each interval", false));
    private final BoolSetting alertHighPing = register(new BoolSetting(
            "Alert High Ping", "Send an alert when a player exceeds the threshold", true));

    private int tickCount = 0;
    private final List<UUID> alreadyAlerted = new ArrayList<>();

    public ConnectionMonitor() {
        super("ConnectionMonitor", "Monitors player connection quality and flags high-ping players", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        tickCount = 0;
        alreadyAlerted.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        tickCount++;
        if (tickCount % updateInterval.get() != 0) return;

        var entries = mc.getNetworkHandler().getPlayerList();
        if (entries == null || entries.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (PlayerListEntry entry : entries) {
            int ping = entry.getLatency();
            String name = entry.getProfile().getName();
            UUID uuid = entry.getProfile().getId();

            if (showAll.isEnabled()) {
                sb.append(name).append(":").append(ping).append("ms  ");
            }

            if (alertHighPing.isEnabled() && ping >= highPingThreshold.get()) {
                if (!alreadyAlerted.contains(uuid)) {
                    ChatUtil.warn("[ConnMonitor] §e" + name + " §7has high ping: §c" + ping + "ms");
                    alreadyAlerted.add(uuid);
                }
            } else {
                alreadyAlerted.remove(uuid);
            }
        }

        if (showAll.isEnabled() && sb.length() > 0) {
            ChatUtil.info("[ConnMonitor] " + sb.toString().trim());
        }
    }
}
