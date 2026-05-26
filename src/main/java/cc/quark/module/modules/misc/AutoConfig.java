package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

// Config profile switcher: saves/loads named config snapshots
public class AutoConfig extends Module {

    private final ModeSetting profile = register(new ModeSetting(
            "Profile", "Config profile to load/save",
            "Default", "Default", "Combat", "Legit", "Crystal"));

    public AutoConfig() {
        super("AutoConfig", "Save and load config profiles", Category.MISC);
    }

    @Override
    public void onEnable() {
        String profileName = profile.get();
        File gameDir       = MinecraftClient.getInstance().runDirectory;
        Path baseConfigs   = gameDir.toPath().resolve("quark").resolve("configs");
        Path profileDir    = gameDir.toPath().resolve("quark").resolve("profiles").resolve(profileName);

        try {
            // Save current config into the profile directory (copy current config files)
            Files.createDirectories(profileDir);
            if (Files.isDirectory(baseConfigs)) {
                try (var stream = Files.list(baseConfigs)) {
                    stream.filter(p -> p.toString().endsWith(".json"))
                          .forEach(src -> {
                              try {
                                  Files.copy(src, profileDir.resolve(src.getFileName()),
                                             StandardCopyOption.REPLACE_EXISTING);
                              } catch (IOException ignored) {}
                          });
                }
            }

            // If profile directory already had a previous save, load it now
            // (on first activation it just saves; subsequent activations load)
            // Logic: if profileDir had files BEFORE this call we load, otherwise we saved fresh
            // Simple approach: always attempt to load back from profileDir into baseConfigs
            if (Files.list(profileDir).findAny().isPresent()) {
                Files.createDirectories(baseConfigs);
                try (var stream = Files.list(profileDir)) {
                    stream.filter(p -> p.toString().endsWith(".json"))
                          .forEach(src -> {
                              try {
                                  Files.copy(src, baseConfigs.resolve(src.getFileName()),
                                             StandardCopyOption.REPLACE_EXISTING);
                              } catch (IOException ignored) {}
                          });
                }
                // Reload modules from the restored config files
                Module.silent = true;
                Quark.getInstance().getConfigManager().load();
                Module.silent = false;
            }
        } catch (IOException e) {
            Quark.LOGGER.warn("AutoConfig: failed for profile '{}': {}", profileName, e.getMessage());
        }

        this.disable(); // one-shot: disable after executing
    }
}
