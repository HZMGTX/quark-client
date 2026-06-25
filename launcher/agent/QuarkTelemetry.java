package cc.quark.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;

/**
 * Opt-in telemetry for the standalone client. Reads the same
 * {@code ~/.quark/stats.properties} file the launcher writes when a Stats
 * Server URL is configured, so launcher and in-game events land under the
 * same anonymous client id. If that file is missing or has no URL, every
 * call here is a silent no-op — nothing is ever sent without the launcher
 * having explicitly opted in.
 */
final class QuarkTelemetry {

    private static final File FILE = new File(System.getProperty("user.home", "."), ".quark/stats.properties");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static volatile String url;
    private static volatile String clientId;
    private static volatile boolean loaded = false;

    private QuarkTelemetry() {}

    private static void loadIfNeeded() {
        if (loaded) return;
        loaded = true;
        if (!FILE.exists()) return;
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(FILE)) {
            p.load(in);
        } catch (IOException ignored) {
            return;
        }
        String u = p.getProperty("url");
        String id = p.getProperty("clientId");
        if (u != null && !u.isBlank() && id != null && !id.isBlank()) {
            url = u.trim();
            clientId = id.trim();
        }
    }

    /** Fire-and-forget; never throws, never blocks the render thread. */
    static void report(String type, Map<String, Object> payload) {
        loadIfNeeded();
        String u = url;
        if (u == null) return;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(u.replaceAll("/+$", "") + "/api/event"))
                    .timeout(Duration.ofSeconds(4))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(toJson(type, payload)))
                    .build();
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding()).exceptionally(e -> null);
        } catch (Throwable ignored) {
        }
    }

    private static String toJson(String type, Map<String, Object> payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"source\":\"client\",\"type\":\"").append(esc(type)).append('"');
        sb.append(",\"clientId\":\"").append(esc(clientId)).append('"');
        if (payload != null && !payload.isEmpty()) {
            sb.append(",\"payload\":{");
            boolean first = true;
            for (Map.Entry<String, Object> e : payload.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                Object v = e.getValue();
                sb.append('"').append(esc(e.getKey())).append("\":");
                if (v instanceof Boolean || v instanceof Number) sb.append(v);
                else sb.append('"').append(esc(String.valueOf(v))).append('"');
            }
            sb.append('}');
        }
        sb.append('}');
        return sb.toString();
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
