package cc.quark.config;

import cc.quark.module.Module;
import cc.quark.module.ModuleManager;
import cc.quark.setting.*;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/**
 * Saves and loads each module's enabled state and settings to/from JSON files.
 * Files are stored in {@code .minecraft/quark/configs/<ModuleName>.json}.
 */
public class ConfigManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("Quark/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final ModuleManager moduleManager;
    private final Path configDir;

    public ConfigManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;

        // Resolve config directory relative to .minecraft folder.
        File gameDir = MinecraftClient.getInstance().runDirectory;
        this.configDir = gameDir.toPath().resolve("quark").resolve("configs");
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Save all modules to individual JSON files.
     */
    public void save() {
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            LOGGER.error("Could not create config directory: {}", e.getMessage());
            return;
        }

        for (Module module : moduleManager.getModules()) {
            saveModule(module);
        }

        LOGGER.info("Config saved ({} modules).", moduleManager.getModules().size());
    }

    /**
     * Load all modules from their respective JSON files.
     * Missing files are silently skipped (module keeps defaults).
     */
    public void load() {
        if (!Files.isDirectory(configDir)) {
            LOGGER.info("No config directory found â€” using defaults.");
            return;
        }

        int loaded = 0;
        for (Module module : moduleManager.getModules()) {
            if (loadModule(module)) loaded++;
        }

        LOGGER.info("Config loaded ({}/{} modules).", loaded, moduleManager.getModules().size());
    }

    // -------------------------------------------------------------------------
    // Per-module helpers
    // -------------------------------------------------------------------------

    private void saveModule(Module module) {
        Path file = configDir.resolve(module.getName() + ".json");
        JsonObject root = new JsonObject();
        root.addProperty("enabled", module.isEnabled());
        root.addProperty("keybind", module.getKeybind());

        JsonObject settingsObj = new JsonObject();
        for (Setting<?> setting : module.getSettings()) {
            settingsObj.add(setting.getName(), settingToJson(setting));
        }
        root.add("settings", settingsObj);

        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save module {}: {}", module.getName(), e.getMessage());
        }
    }

    private boolean loadModule(Module module) {
        Path file = configDir.resolve(module.getName() + ".json");
        if (!Files.exists(file)) return false;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("enabled")) {
                boolean shouldEnable = root.get("enabled").getAsBoolean();
                if (shouldEnable && !module.isEnabled()) module.enable();
                else if (!shouldEnable && module.isEnabled()) module.disable();
            }

            if (root.has("keybind")) {
                module.setKeybind(root.get("keybind").getAsInt());
            }

            if (root.has("settings")) {
                JsonObject settingsObj = root.getAsJsonObject("settings");
                for (Setting<?> setting : module.getSettings()) {
                    if (settingsObj.has(setting.getName())) {
                        applySettingJson(setting, settingsObj.get(setting.getName()));
                    }
                }
            }

            return true;
        } catch (Exception e) {
            LOGGER.warn("Failed to load module {}: {}", module.getName(), e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // JSON serialisation helpers
    // -------------------------------------------------------------------------

    private JsonElement settingToJson(Setting<?> setting) {
        Object val = setting.getValue();
        if (val instanceof Boolean b)  return new JsonPrimitive(b);
        if (val instanceof Number n)   return new JsonPrimitive(n);
        if (val instanceof String s)   return new JsonPrimitive(s);
        if (val instanceof Enum<?> e)  return new JsonPrimitive(e.name());
        return new JsonPrimitive(String.valueOf(val));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void applySettingJson(Setting<?> setting, JsonElement element) {
        try {
            if (setting instanceof BoolSetting bs) {
                bs.setValue(element.getAsBoolean());
            } else if (setting instanceof DoubleSetting ds) {
                ds.setValue(element.getAsDouble());
            } else if (setting instanceof IntSetting is) {
                is.setValue(element.getAsInt());
            } else if (setting instanceof EnumSetting es) {
                String name = element.getAsString();
                Object[] constants = es.getValues();
                for (Object constant : constants) {
                    if (((Enum<?>) constant).name().equalsIgnoreCase(name)) {
                        es.setValue((Enum) constant);
                        break;
                    }
                }
            } else if (setting instanceof ModeSetting ms) {
                ms.setValue(element.getAsString());
            } else if (setting instanceof ColorSetting cs) {
                cs.setValue(element.getAsInt());
            }
        } catch (Exception e) {
            LOGGER.warn("Could not apply setting {}: {}", setting.getName(), e.getMessage());
        }
    }
}
