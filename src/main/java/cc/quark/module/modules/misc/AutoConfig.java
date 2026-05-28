package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

import java.io.IOException;
import java.nio.file.*;

public class AutoConfig extends Module {

    private final ModeSetting profile = register(new ModeSetting(
            "Profile", "Config profile to auto-load on server join",
            "Default", "Default", "PvP", "Survival", "Hypixel", "Mineplex"));

    private boolean waitingForJoin = false;
    private boolean loaded = false;

    public AutoConfig() {
        super("AutoConfig", "Auto-loads a config profile on first tick after joining a server", Category.MISC);
    }

    @Override
    public void onEnable() {
        waitingForJoin = true;
        loaded = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            loaded = false;
            waitingForJoin = true;
            return;
        }

        if (!waitingForJoin || loaded) return;
        loaded = true;
        waitingForJoin = false;

        String serverAddress = "default";
        ServerInfo si = mc.getCurrentServerEntry();
        if (si != null && si.address != null && !si.address.isEmpty()) {
            serverAddress = si.address.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
        }

        String profileName = resolveProfile(profile.get(), serverAddress);
        loadProfile(mc, profileName);
    }

    private String resolveProfile(String manual, String address) {
        if (!manual.equals("Default")) return manual;
        if (address.contains("hypixel")) return "Hypixel";
        if (address.contains("mineplex")) return "Mineplex";
        return "Default";
    }

    private void loadProfile(MinecraftClient mc, String profileName) {
        Path gameDir = mc.runDirectory.toPath();
        Path profileDir = gameDir.resolve("quark").resolve("profiles").resolve(profileName);
        Path baseConfigs = gameDir.resolve("quark").resolve("configs");

        if (!Files.isDirectory(profileDir)) {
            ChatUtil.info("AutoConfig: no profile '" + profileName + "' found, saving current config.");
            saveCurrentToProfile(profileDir, baseConfigs);
            return;
        }

        try {
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
            Module.silent = true;
            Quark.getInstance().getConfigManager().load();
            Module.silent = false;
            ChatUtil.success("AutoConfig: loaded profile '" + profileName + "'.");
        } catch (IOException e) {
            Quark.LOGGER.warn("AutoConfig: failed to load profile '{}': {}", profileName, e.getMessage());
        }
    }

    private void saveCurrentToProfile(Path profileDir, Path baseConfigs) {
        try {
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
        } catch (IOException e) {
            Quark.LOGGER.warn("AutoConfig: failed to save profile: {}", e.getMessage());
        }
    }
}
