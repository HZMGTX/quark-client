package cc.quark.module.modules.staff;

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
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class IPLogger extends Module {

    private final BoolSetting alertInChat = register(new BoolSetting(
            "Alert In Chat", "Show player join/server IP info in chat", true));
    private final BoolSetting logToFile = register(new BoolSetting(
            "Log To File", "Write server connection info to iplog.txt", true));

    private String lastServer = null;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public IPLogger() {
        super("IPLogger", "Logs server IP and player list info to file", Category.STAFF, 0);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // Log the server address on first connection
        var serverInfo = mc.getCurrentServerEntry();
        if (serverInfo != null && !serverInfo.address.equals(lastServer)) {
            lastServer = serverInfo.address;
            String entry = "[" + LocalDateTime.now().format(FMT) + "] Connected to: " + serverInfo.address
                         + " (" + serverInfo.name + ")";
            if (alertInChat.isEnabled()) ChatUtil.info("[IPLogger] Server: " + serverInfo.address);
            if (logToFile.isEnabled()) writeLog(entry);
        }
    }

    private void writeLog(String line) {
        try {
            Path logDir = mc.runDirectory.toPath().resolve("quark");
            Files.createDirectories(logDir);
            try (PrintWriter pw = new PrintWriter(new FileWriter(logDir.resolve("iplog.txt").toFile(), true))) {
                pw.println(line);
            }
        } catch (IOException ignored) {}
    }
}
