package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Broadcasts cheat detection alerts to the staff channel.
 * Other modules call CheatBroadcast.broadcast() to queue a message.
 * This module drains the queue each tick and sends them via the configured channel command.
 */
public class CheatBroadcast extends Module {

    private final StringSetting broadcastChannel = register(new StringSetting(
            "Broadcast Channel", "Staff chat channel prefix/command to use", "#staff"));
    private final ModeSetting minSeverity = register(new ModeSetting(
            "Min Severity", "Minimum severity level to broadcast", "Medium", "Low", "Medium", "High"));

    private static final Deque<BroadcastEntry> queue = new ArrayDeque<>();
    private static final Object QUEUE_LOCK = new Object();
    private static CheatBroadcast instance;

    public CheatBroadcast() {
        super("CheatBroadcast", "Broadcasts cheat detection alerts to all online staff members", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        instance = this;
        synchronized (QUEUE_LOCK) { queue.clear(); }
        ChatUtil.info("§6[CheatBroadcast] §fBroadcasting to §e" + broadcastChannel.get()
                + " §f(min severity: §e" + minSeverity.get() + "§f).");
    }

    @Override
    public void onDisable() {
        instance = null;
        synchronized (QUEUE_LOCK) { queue.clear(); }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Drain one message per tick to avoid chat flooding
        BroadcastEntry entry;
        synchronized (QUEUE_LOCK) {
            entry = queue.pollFirst();
        }
        if (entry == null) return;

        if (!meetsMinSeverity(entry.severity)) return;

        String severityColor = switch (entry.severity) {
            case "High"   -> "§c";
            case "Medium" -> "§e";
            default       -> "§f";
        };

        String channel = instance != null ? instance.broadcastChannel.get() : "#staff";
        String msg = severityColor + "[CHEAT-ALERT " + entry.severity + "] §f" + entry.message;

        // Send to staff channel via command, then echo locally
        mc.player.networkHandler.sendChatCommand(channel.replace("#", "") + " " + msg);
        ChatUtil.warn("§6[CheatBroadcast] §fSent: " + msg);
    }

    /**
     * Queue a broadcast message from any detection module.
     *
     * @param message  The formatted alert message.
     * @param severity "Low", "Medium", or "High".
     */
    public static void broadcast(String message, String severity) {
        synchronized (QUEUE_LOCK) {
            // Cap queue at 50 to prevent memory growth during spikes
            if (queue.size() < 50) {
                queue.addLast(new BroadcastEntry(message, severity));
            }
        }
    }

    private boolean meetsMinSeverity(String severity) {
        int required = severityLevel(minSeverity.get());
        return severityLevel(severity) >= required;
    }

    private static int severityLevel(String s) {
        return switch (s) {
            case "High"   -> 2;
            case "Medium" -> 1;
            default       -> 0;
        };
    }

    private static class BroadcastEntry {
        final String message;
        final String severity;

        BroadcastEntry(String message, String severity) {
            this.message = message;
            this.severity = severity;
        }
    }
}
