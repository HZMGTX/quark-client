package cc.quark.agent;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Persists StandaloneClient state across game sessions — module toggles plus
 * scalar settings such as the ClickGUI scale. Stored as a flat properties file
 * under the user's home directory so it survives reinjection without needing
 * the real Fabric mod's config system.
 *
 * Values are kept as raw strings; callers decide how to interpret each key
 * (booleans for module state, numbers for sliders).
 */
final class QuarkConfig {

    private static final File FILE = new File(System.getProperty("user.home", "."), ".quark/quark.properties");

    private QuarkConfig() {}

    static Map<String, String> load() {
        Map<String, String> out = new LinkedHashMap<>();
        if (!FILE.exists()) return out;
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(FILE)) {
            p.load(in);
        } catch (IOException ignored) {
            return out;
        }
        for (String key : p.stringPropertyNames()) {
            out.put(key, p.getProperty(key));
        }
        return out;
    }

    static void save(Map<String, String> values) {
        Properties p = new Properties();
        for (Map.Entry<String, String> e : values.entrySet()) {
            p.setProperty(e.getKey(), e.getValue());
        }
        try {
            File parent = FILE.getParentFile();
            if (parent != null) parent.mkdirs();
            try (OutputStream out = new FileOutputStream(FILE)) {
                p.store(out, "Quark client state");
            }
        } catch (IOException ignored) {}
    }
}
