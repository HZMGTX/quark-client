package cc.quark.module.modules.player;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * PacketLogger - logs incoming and outgoing packets to chat and/or file.
 *
 * FilterMovement skips the extremely frequent PlayerMoveC2SPacket to keep
 * output readable. WriteFile saves entries to .minecraft/quark/packets.log.
 */
public class PacketLogger extends Module {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final BoolSetting logIncoming = register(new BoolSetting(
            "Log Incoming", "Log packets received from server", true));

    private final BoolSetting logOutgoing = register(new BoolSetting(
            "Log Outgoing", "Log packets sent to server", true));

    private final BoolSetting filterMovement = register(new BoolSetting(
            "Filter Movement", "Skip PlayerMoveC2SPacket spam", true));

    private final BoolSetting writeFile = register(new BoolSetting(
            "Write File", "Write log to .minecraft/quark/packets.log", false));

    private FileWriter fileWriter;

    public PacketLogger() {
        super("PacketLogger", "Logs all packets for debugging", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (writeFile.isEnabled()) openFile();
    }

    @Override
    public void onDisable() {
        closeFile();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!logOutgoing.isEnabled()) return;
        if (filterMovement.isEnabled() && event.getPacket() instanceof PlayerMoveC2SPacket) return;
        String name = event.getPacket().getClass().getSimpleName();
        logEntry("OUT", name);
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!logIncoming.isEnabled()) return;
        String name = event.getPacket().getClass().getSimpleName();
        logEntry("IN", name);
    }

    private void logEntry(String direction, String packetName) {
        String timestamp = "[" + LocalTime.now().format(TIME_FMT) + "]";
        String line = timestamp + " [" + direction + "] " + packetName;

        // Log to console
        Quark.LOGGER.info("[PacketLogger] {}", line);

        // Log to chat (shortened to avoid spam)
        ChatUtil.info("[" + direction + "] " + packetName);

        // Log to file if enabled
        if (writeFile.isEnabled()) {
            if (fileWriter == null) openFile();
            if (fileWriter != null) {
                try {
                    fileWriter.write(line + System.lineSeparator());
                    fileWriter.flush();
                } catch (IOException ignored) {}
            }
        }
    }

    private void openFile() {
        try {
            Path logFile = mc.runDirectory.toPath().resolve("quark").resolve("packets.log");
            Files.createDirectories(logFile.getParent());
            fileWriter = new FileWriter(logFile.toFile(), true);
        } catch (IOException e) {
            fileWriter = null;
        }
    }

    private void closeFile() {
        if (fileWriter != null) {
            try { fileWriter.close(); } catch (IOException ignored) {}
            fileWriter = null;
        }
    }
}
