package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * DiscordWebhook - Sends configurable events to a Discord webhook URL.
 * Supports chat messages, health alerts, and periodic status pings.
 */
public class DiscordWebhook extends Module {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final StringSetting webhookUrl = register(new StringSetting(
            "Webhook URL", "Discord webhook URL to post to", "https://discord.com/api/webhooks/..."));

    private final StringSetting username = register(new StringSetting(
            "Username", "Bot display name in Discord", "Quark.cc"));

    private final BoolSetting relayChat = register(new BoolSetting(
            "Relay Chat", "Send incoming chat messages to Discord", true));

    private final BoolSetting healthAlert = register(new BoolSetting(
            "Health Alert", "Send alert when health drops below threshold", true));

    private final IntSetting healthThreshold = register(new IntSetting(
            "Health Threshold", "HP level to trigger health alert", 6, 1, 20));

    private final BoolSetting statusPing = register(new BoolSetting(
            "Status Ping", "Periodically send a status ping to Discord", false));

    private final IntSetting pingIntervalSec = register(new IntSetting(
            "Ping Interval", "Seconds between status pings", 300, 30, 3600));

    private final BoolSetting onlyMentioned = register(new BoolSetting(
            "Only Mentioned", "Only relay chat messages that mention the player name", false));

    private final ExecutorService executor = Executors.newSingleThreadExecutor(
            r -> { Thread t = new Thread(r, "quark-webhook"); t.setDaemon(true); return t; });

    private long lastPingMs = 0;
    private boolean healthAlerted = false;

    public DiscordWebhook() {
        super("DiscordWebhook", "Sends game events to a Discord webhook URL", Category.MISC);
    }

    @Override
    public void onEnable() {
        lastPingMs = System.currentTimeMillis();
        healthAlerted = false;
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (!relayChat.isEnabled()) return;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;

        if (onlyMentioned.isEnabled() && mc.player != null) {
            String playerName = mc.player.getName().getString().toLowerCase();
            if (!msg.toLowerCase().contains(playerName)) return;
        }

        String stripped = stripColor(msg);
        sendAsync("[" + FMT.format(LocalDateTime.now()) + "] `" + escapeJson(stripped) + "`");
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Health alert
        if (healthAlert.isEnabled()) {
            float hp = mc.player.getHealth();
            if (hp <= healthThreshold.get() && !healthAlerted) {
                healthAlerted = true;
                String name = mc.player.getName().getString();
                sendAsync("**HEALTH ALERT** `" + name + "` is at **" + String.format("%.1f", hp) + " HP**! ["
                        + FMT.format(LocalDateTime.now()) + "]");
            } else if (hp > healthThreshold.get() + 2) {
                healthAlerted = false; // reset when health recovers
            }
        }

        // Periodic status ping
        if (statusPing.isEnabled()) {
            long now = System.currentTimeMillis();
            if (now - lastPingMs >= pingIntervalSec.get() * 1000L) {
                lastPingMs = now;
                String name = mc.player.getName().getString();
                double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
                String dim  = mc.world != null ? mc.world.getRegistryKey().getValue().getPath() : "unknown";
                sendAsync(String.format("**Status** | `%s` | X: %.0f Y: %.0f Z: %.0f | %s | HP: %.1f",
                        name, x, y, z, dim, mc.player.getHealth()));
            }
        }
    }

    private void sendAsync(String content) {
        String url = webhookUrl.get().trim();
        if (url.isEmpty() || url.startsWith("https://discord.com/api/webhooks/...")) {
            ChatUtil.warn("[DiscordWebhook] Webhook URL not configured.");
            return;
        }

        String displayName = escapeJson(username.get().trim().isEmpty() ? "Quark.cc" : username.get().trim());
        String escapedContent = escapeJson(content);

        String payload = "{\"username\":\"" + displayName + "\","
                + "\"content\":\"" + escapedContent + "\"}";

        executor.submit(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                int code = conn.getResponseCode();
                if (code >= 400) {
                    ChatUtil.warn("[DiscordWebhook] HTTP " + code);
                }
                conn.disconnect();
            } catch (Exception e) {
                ChatUtil.error("[DiscordWebhook] " + e.getMessage());
            }
        });
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String stripColor(String s) {
        return s.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}
