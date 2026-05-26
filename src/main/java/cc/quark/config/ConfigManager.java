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
import java.util.ArrayList;
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
        
        saveGui();

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

        Module.silent = true; // suppress notifications during config load
        int loaded = 0;
        for (Module module : moduleManager.getModules()) {
            if (loadModule(module)) loaded++;
        }
        Module.silent = false;

        loadGui();

        LOGGER.info("Config loaded ({}/{} modules).", loaded, moduleManager.getModules().size());
    }

    // -------------------------------------------------------------------------
    // Profile API
    // -------------------------------------------------------------------------

    public void saveProfile(String name) {
        Path profileDir = getProfileDir(name);
        try {
            Files.createDirectories(profileDir);
        } catch (IOException e) {
            LOGGER.error("Could not create profile directory '{}': {}", name, e.getMessage());
            return;
        }
        for (Module module : moduleManager.getModules()) {
            saveModuleTo(module, profileDir);
        }
        LOGGER.info("Profile '{}' saved.", name);
    }

    public void loadProfile(String name) {
        Path profileDir = getProfileDir(name);
        if (!Files.isDirectory(profileDir)) {
            LOGGER.warn("Profile '{}' not found.", name);
            return;
        }
        Module.silent = true;
        for (Module module : moduleManager.getModules()) {
            loadModuleFrom(module, profileDir);
        }
        Module.silent = false;
        LOGGER.info("Profile '{}' loaded.", name);
    }

    public List<String> listProfiles() {
        Path profilesRoot = getProfilesRoot();
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(profilesRoot)) return names;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(profilesRoot)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    names.add(entry.getFileName().toString());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to list profiles: {}", e.getMessage());
        }
        return names;
    }

    public boolean deleteProfile(String name) {
        Path profileDir = getProfileDir(name);
        if (!Files.isDirectory(profileDir)) return false;
        try {
            try (var walk = Files.walk(profileDir)) {
                walk.sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            LOGGER.info("Profile '{}' deleted.", name);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to delete profile '{}': {}", name, e.getMessage());
            return false;
        }
    }

    private Path getProfilesRoot() {
        File gameDir = MinecraftClient.getInstance().runDirectory;
        return gameDir.toPath().resolve("quark").resolve("profiles");
    }

    private Path getProfileDir(String name) {
        return getProfilesRoot().resolve(name);
    }

    private void saveModuleTo(Module module, Path dir) {
        Path file = dir.resolve(module.getName() + ".json");
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
            LOGGER.error("Failed to save module {} to profile: {}", module.getName(), e.getMessage());
        }
    }

    private void loadModuleFrom(Module module, Path dir) {
        Path file = dir.resolve(module.getName() + ".json");
        if (!Files.exists(file)) return;
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
        } catch (Exception e) {
            LOGGER.warn("Failed to load module {} from profile: {}", module.getName(), e.getMessage());
        }
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

    private void saveGui() {
        Path file = configDir.resolve("ClickGUI.json");
        JsonObject root = new JsonObject();
        
        for (cc.quark.gui.components.CategoryPanel panel : cc.quark.gui.ClickGUI.getPanels()) {
            JsonObject panelObj = new JsonObject();
            panelObj.addProperty("x", panel.getX());
            panelObj.addProperty("y", panel.getY());
            root.add(panel.getCategory().name(), panelObj);
        }
        
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save ClickGUI layout: {}", e.getMessage());
        }
    }

    private void loadGui() {
        Path file = configDir.resolve("ClickGUI.json");
        if (!Files.exists(file)) return;

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            
            // If panels aren't initialized yet, we must initialize them to load positions
            if (cc.quark.gui.ClickGUI.getPanels().isEmpty()) {
                // We create a dummy GUI screen to force init
                new cc.quark.gui.ClickGUI().init(MinecraftClient.getInstance(), 800, 600);
            }

            for (cc.quark.gui.components.CategoryPanel panel : cc.quark.gui.ClickGUI.getPanels()) {
                if (root.has(panel.getCategory().name())) {
                    JsonObject panelObj = root.getAsJsonObject(panel.getCategory().name());
                    if (panelObj.has("x")) panel.setX(panelObj.get("x").getAsInt());
                    if (panelObj.has("y")) panel.setY(panelObj.get("y").getAsInt());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load ClickGUI layout: {}", e.getMessage());
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
