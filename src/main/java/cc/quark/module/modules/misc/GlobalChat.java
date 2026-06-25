package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.text.Text;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

/**
 * GlobalChat — connects to a WebSocket server and bridges Minecraft chat
 * with a shared cross-server channel.
 *
 * Usage:
 *   - Enable the module to connect to the configured WebSocket server.
 *   - Send a message starting with '#' to broadcast it to the global channel.
 *   - Incoming messages are displayed as: [GlobalChat] <username>: <message>
 */
public class GlobalChat extends Module {

    private final StringSetting serverUrl  = register(new StringSetting("Server URL",   "WebSocket server URI",                                   "wss://quark-chat.glitch.me"));
    private final ModeSetting   channel    = register(new ModeSetting("Channel",        "Chat channel to join",                                   "Global", "Global", "PvP", "Trade", "Staff"));
    private final BoolSetting   showPing   = register(new BoolSetting("Show Ping",      "Display [PING] message when WebSocket connects",         true));
    private final BoolSetting   filterSpam = register(new BoolSetting("Filter Spam",    "Suppress duplicate incoming messages within 3 seconds",  true));

    // WebSocket state
    private volatile WebSocket  webSocket   = null;
    private volatile boolean    connected   = false;

    // Spam filter state
    private final Set<String>   recentMessages = new HashSet<>();
    private long                lastSpamCleanup = 0L;

    // Used to avoid re-entrancy when we programmatically send via the network packet
    private volatile boolean    suppressOutgoingCancel = false;

    public GlobalChat() {
        super("GlobalChat", "Connects to a WebSocket chat server for cross-server messaging", Category.MISC);
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void onEnable() {
        connectWebSocket();
    }

    @Override
    public void onDisable() {
        disconnectWebSocket();
    }

    // -------------------------------------------------------------------------
    // WebSocket connection
    // -------------------------------------------------------------------------

    private void connectWebSocket() {
        String url = serverUrl.get();
        if (url == null || url.isBlank()) {
            sendInfo("§cGlobalChat: Server URL is not configured.");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        try {
            URI uri = URI.create(url);
            client.newWebSocketBuilder()
                    .buildAsync(uri, new GlobalChatListener())
                    .whenComplete((ws, ex) -> {
                        if (ex != null) {
                            sendInfo("§cGlobalChat: Failed to connect — " + ex.getMessage());
                        }
                        // ws is stored in the listener's onOpen callback
                    });
        } catch (IllegalArgumentException e) {
            sendInfo("§cGlobalChat: Invalid server URL — " + e.getMessage());
        }
    }

    private void disconnectWebSocket() {
        WebSocket ws = webSocket;
        if (ws != null && connected) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "Module disabled").join();
            } catch (Exception ignored) {}
        }
        webSocket = null;
        connected = false;
    }

    // -------------------------------------------------------------------------
    // Outgoing chat intercept
    // -------------------------------------------------------------------------

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming()) return;
        if (suppressOutgoingCancel) return;
        if (!connected || webSocket == null) return;

        String msg = event.getMessage();
        if (msg == null || !msg.startsWith("#")) return;

        // Strip the leading '#' and trim
        String text = msg.substring(1).trim();
        if (text.isEmpty()) return;

        // Cancel the original outgoing message so it isn't sent to the MC server
        event.cancel();

        // Broadcast to WebSocket server
        String username = getPlayerName();
        String json = buildJson("msg", username, text);
        sendToServer(json);
    }

    // -------------------------------------------------------------------------
    // JSON helpers (no external library — hand-rolled)
    // -------------------------------------------------------------------------

    /** Builds a join packet. */
    private String buildJoinJson(String username) {
        return "{\"type\":\"join\","
                + "\"username\":" + jsonString(username) + ","
                + "\"channel\":" + jsonString(channel.get())
                + "}";
    }

    /** Builds a msg packet. */
    private String buildJson(String type, String username, String text) {
        return "{\"type\":" + jsonString(type) + ","
                + "\"username\":" + jsonString(username) + ","
                + "\"channel\":" + jsonString(channel.get()) + ","
                + "\"text\":" + jsonString(text)
                + "}";
    }

    /** Minimal JSON string escaping for single values. */
    private String jsonString(String value) {
        if (value == null) return "\"\"";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    /**
     * Very simple JSON value extractor — finds the value of a top-level key
     * in a flat JSON object. Not a full parser; sufficient for the simple
     * messages this server sends.
     */
    private String jsonGet(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return null;
        int colon = json.indexOf(':', keyIdx + search.length());
        if (colon == -1) return null;
        int valueStart = colon + 1;
        // skip whitespace
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart >= json.length()) return null;
        char first = json.charAt(valueStart);
        if (first == '"') {
            // string value
            StringBuilder sb = new StringBuilder();
            int i = valueStart + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    switch (next) {
                        case '"':  sb.append('"');  i += 2; break;
                        case '\\': sb.append('\\'); i += 2; break;
                        case 'n':  sb.append('\n'); i += 2; break;
                        case 'r':  sb.append('\r'); i += 2; break;
                        case 't':  sb.append('\t'); i += 2; break;
                        default:   sb.append(next); i += 2; break;
                    }
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                    i++;
                }
            }
            return sb.toString();
        } else {
            // non-string value (number, bool, null) — read until delimiter
            int end = valueStart;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) break;
                end++;
            }
            return json.substring(valueStart, end);
        }
    }

    // -------------------------------------------------------------------------
    // Utility helpers
    // -------------------------------------------------------------------------

    private String getPlayerName() {
        if (mc != null && mc.player != null) {
            return mc.player.getName().getString();
        }
        return "Player";
    }

    private void sendToServer(String json) {
        WebSocket ws = webSocket;
        if (ws == null || !connected) return;
        try {
            ws.sendText(json, true);
        } catch (Exception e) {
            sendInfo("§cGlobalChat: Send failed — " + e.getMessage());
        }
    }

    /** Thread-safe wrapper to display a message in the MC chat HUD. */
    private void sendInfo(String msg) {
        if (mc == null) return;
        if (mc.player != null) {
            // We may be on a background thread — schedule on the main thread
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.sendMessage(Text.literal(msg), false);
                }
            });
        }
    }

    // -------------------------------------------------------------------------
    // Spam filter
    // -------------------------------------------------------------------------

    /**
     * Returns true if the message should be suppressed as spam.
     * Also periodically clears the recent-messages set.
     */
    private boolean isSpam(String key) {
        long now = System.currentTimeMillis();
        if (now - lastSpamCleanup > 3000L) {
            recentMessages.clear();
            lastSpamCleanup = now;
        }
        return !recentMessages.add(key);
    }

    // -------------------------------------------------------------------------
    // WebSocket listener (inner class)
    // -------------------------------------------------------------------------

    private class GlobalChatListener implements WebSocket.Listener {

        private final StringBuilder messageBuffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket ws) {
            webSocket = ws;
            connected = true;

            // Send join packet
            String joinJson = buildJoinJson(getPlayerName());
            ws.sendText(joinJson, true);

            if (showPing.isEnabled()) {
                sendInfo("§aGlobalChat: Connected to " + serverUrl.get() + " [" + channel.get() + "]");
            }

            // Request first message
            ws.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            messageBuffer.append(data);
            if (last) {
                String fullMessage = messageBuffer.toString();
                messageBuffer.setLength(0);
                handleMessage(fullMessage);
            }
            ws.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket ws, ByteBuffer data, boolean last) {
            ws.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            connected = false;
            webSocket = null;
            if (isEnabled()) {
                sendInfo("§eGlobalChat: Disconnected (code=" + statusCode + (reason.isEmpty() ? "" : ", " + reason) + ")");
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket ws, Throwable error) {
            connected = false;
            webSocket = null;
            sendInfo("§cGlobalChat: WebSocket error — " + error.getMessage());
        }

        private void handleMessage(String json) {
            try {
                String type     = jsonGet(json, "type");
                String username = jsonGet(json, "username");
                String text     = jsonGet(json, "text");

                if (type == null) return;

                // Ignore echo of our own join packet or server acks with no text
                if ("msg".equals(type)) {
                    if (username == null) username = "?";
                    if (text == null || text.isEmpty()) return;

                    String spamKey = username + ":" + text;
                    if (filterSpam.isEnabled() && isSpam(spamKey)) return;

                    String displayMsg = "§a[GlobalChat] §f" + username + "§7: §r" + text;
                    sendInfo(displayMsg);

                } else if ("ping".equals(type) || "pong".equals(type)) {
                    if (showPing.isEnabled()) {
                        sendInfo("§7[GlobalChat] [" + type.toUpperCase() + "]");
                    }

                } else if ("join".equals(type)) {
                    if (username != null && !username.equals(getPlayerName())) {
                        sendInfo("§7[GlobalChat] §e" + username + "§7 joined §e" + channel.get());
                    }

                } else if ("leave".equals(type)) {
                    if (username != null && !username.equals(getPlayerName())) {
                        sendInfo("§7[GlobalChat] §e" + username + "§7 left §e" + channel.get());
                    }
                }
                // Unknown message types are silently ignored

            } catch (Exception e) {
                // Malformed JSON or unexpected server message — ignore silently
            }
        }
    }
}
