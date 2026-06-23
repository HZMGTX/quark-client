package cc.quark.agent;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Persists StandaloneClient module toggle state across game sessions.
 * Stored as a flat properties file under the user's home directory so it
 * survives reinjection without needing the real Fabric mod's config system.
 */
final class QuarkConfig {

    private static final File FILE = new File(System.getProperty("user.home", "."), ".quark/quark.properties");

    private QuarkConfig() {}

    static Map<String, Boolean> load() {
        Map<String, Boolean> out = new HashMap<>();
        if (!FILE.exists()) return out;
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(FILE)) {
            p.load(in);
        } catch (IOException ignored) {
            return out;
        }
        for (String key : p.stringPropertyNames()) {
            out.put(key, Boolean.parseBoolean(p.getProperty(key)));
        }
        return out;
    }

    static void save(Map<String, Boolean> states) {
        Properties p = new Properties();
        for (Map.Entry<String, Boolean> e : states.entrySet()) {
            p.setProperty(e.getKey(), String.valueOf(e.getValue()));
        }
        try {
            File parent = FILE.getParentFile();
            if (parent != null) parent.mkdirs();
            try (OutputStream out = new FileOutputStream(FILE)) {
                p.store(out, "Quark client module state");
            }
        } catch (IOException ignored) {}
    }
}
