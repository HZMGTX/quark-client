package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class PlayerLogger extends Module {
    private final BoolSetting logToFile = register(new BoolSetting("Log To File", "Write to players.log", true));
    private final BoolSetting showInChat = register(new BoolSetting("Chat Alerts", "Show join/leave in chat", true));

    private final Set<String> lastSeen = new HashSet<>();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public PlayerLogger() {
        super("Player Logger", "Logs all players joining and leaving the server", Category.MISC, 0);
    }

    @Override
    public void onEnable() { lastSeen.clear(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        Set<String> current = new HashSet<>();
        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList()) {
            current.add(e.getProfile().getName());
        }

        for (String name : current) {
            if (!lastSeen.contains(name) && !name.equals(mc.player.getName().getString())) {
                String entry = "[" + LocalDateTime.now().format(FMT) + "] JOIN: " + name;
                if (showInChat.isEnabled()) ChatUtil.info("§a[+] §f" + name);
                if (logToFile.isEnabled()) writeLog(entry);
            }
        }
        for (String name : lastSeen) {
            if (!current.contains(name)) {
                String entry = "[" + LocalDateTime.now().format(FMT) + "] LEAVE: " + name;
                if (showInChat.isEnabled()) ChatUtil.info("§c[-] §f" + name);
                if (logToFile.isEnabled()) writeLog(entry);
            }
        }

        lastSeen.clear();
        lastSeen.addAll(current);
    }

    private void writeLog(String line) {
        try {
            Path dir = mc.runDirectory.toPath().resolve("quark");
            Files.createDirectories(dir);
            try (PrintWriter pw = new PrintWriter(new FileWriter(dir.resolve("players.log").toFile(), true))) {
                pw.println(line);
            }
        } catch (IOException ignored) {}
    }
}
