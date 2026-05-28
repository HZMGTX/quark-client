package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatLogger extends Module {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BoolSetting logIncoming  = register(new BoolSetting("Log Incoming", "Log messages received from server", true));
    private final BoolSetting logOutgoing  = register(new BoolSetting("Log Outgoing", "Log messages sent by the player", false));
    private final BoolSetting includeSystem = register(new BoolSetting("Include System", "Log system/server messages", true));
    private final BoolSetting timestamp    = register(new BoolSetting("Timestamp", "Prepend timestamp to each log entry", true));

    private FileWriter writer;

    public ChatLogger() {
        super("ChatLogger", "Logs all chat to ~/.minecraft/quark/chat.log with timestamps", Category.MISC);
    }

    @Override
    public void onEnable() {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            Path logFile = mc.runDirectory.toPath().resolve("quark").resolve("chat.log");
            Files.createDirectories(logFile.getParent());
            writer = new FileWriter(logFile.toFile(), true);
        } catch (IOException e) {
            writer = null;
        }
    }

    @Override
    public void onDisable() {
        if (writer != null) {
            try { writer.close(); } catch (IOException ignored) {}
            writer = null;
        }
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (writer == null) return;
        if (event.isIncoming() && !logIncoming.isEnabled()) return;
        if (!event.isIncoming() && !logOutgoing.isEnabled()) return;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;

        String direction = event.isIncoming() ? "[IN]" : "[OUT]";
        String line;
        if (timestamp.isEnabled()) {
            line = "[" + LocalDateTime.now().format(TIME_FMT) + "] " + direction + " " + msg;
        } else {
            line = direction + " " + msg;
        }

        try {
            writer.write(line + System.lineSeparator());
            writer.flush();
        } catch (IOException ignored) {}
    }
}
