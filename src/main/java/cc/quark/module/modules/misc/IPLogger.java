package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.network.ServerInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * IPLogger - logs server connection info for session tracking.
 * Educational use: helps track which servers you've joined.
 */
public class IPLogger extends Module {

    private final BoolSetting autoLog = register(new BoolSetting(
            "Auto Log", "Automatically log server IP on connection", true));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String lastLoggedServer = "";

    public IPLogger() {
        super("IPLogger", "Logs server IPs to file", Category.MISC);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoLog.isEnabled()) return;
        if (mc.player == null || mc.world == null) return;

        String serverAddress = getServerAddress();
        if (serverAddress.isEmpty() || serverAddress.equals(lastLoggedServer)) return;

        lastLoggedServer = serverAddress;
        logServer(serverAddress);
    }

    @Override
    public void onDisable() {
        lastLoggedServer = "";
    }

    private String getServerAddress() {
        // Check current server info
        if (mc.getCurrentServerEntry() != null) {
            return mc.getCurrentServerEntry().address;
        }
        // In-game fallback via network handler
        if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getConnection() != null) {
            var addr = mc.getNetworkHandler().getConnection().getAddress();
            if (addr != null) return addr.toString();
        }
        return "";
    }

    private void logServer(String address) {
        try {
            Path logFile = mc.runDirectory.toPath().resolve("quark").resolve("servers.log");
            Files.createDirectories(logFile.getParent());
            try (FileWriter fw = new FileWriter(logFile.toFile(), true)) {
                fw.write("[" + LocalDateTime.now().format(FMT) + "] Connected: " + address
                        + System.lineSeparator());
            }
        } catch (IOException ignored) {}
    }
}
