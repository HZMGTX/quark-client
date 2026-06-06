package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

import java.util.HashMap;
import java.util.Map;

public class AutoMute2 extends Module {
    private final IntSetting spamThreshold = register(new IntSetting("SpamThreshold", "Messages per second to mute", 5, 2, 20));
    private final StringSetting muteCommand = register(new StringSetting("MuteCmd", "Mute command format (%p = player)", "/mute %p 5m spam"));
    private final BoolSetting logMutes = register(new BoolSetting("LogMutes", "Log auto-mutes to chat", true));
    private final Map<String, Integer> msgCount = new HashMap<>();
    private long lastReset = 0;

    public AutoMute2() { super("AutoMute2", "Automatically mutes spamming players", Category.STAFF); }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        long now = System.currentTimeMillis();
        if (now - lastReset > 1000) { msgCount.clear(); lastReset = now; }
        // Extract player name from message (simplified)
        String msg = event.getMessage();
        if (msg.contains(": ")) {
            String player = msg.split(": ")[0].replaceAll("[<>\\[\\]]", "").trim();
            int count = msgCount.getOrDefault(player, 0) + 1;
            msgCount.put(player, count);
            if (count >= spamThreshold.getValue()) {
                String cmd = muteCommand.getValue().replace("%p", player);
                ChatUtil.send(cmd);
                if (logMutes.getValue()) ChatUtil.warn("[AutoMute] Muted " + player);
                msgCount.put(player, 0);
            }
        }
    }
}
