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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatLogger extends Module {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final BoolSetting includeOutgoing = register(new BoolSetting(
            "Include Outgoing", "Also log messages sent by the player", false));

    private final BoolSetting timestamp = register(new BoolSetting(
            "Timestamp", "Prepend HH:mm:ss timestamp to each log entry", true));

    private FileWriter writer;

    public ChatLogger() {
        super("ChatLogger", "Logs chat messages to .minecraft/quark/chat.log", Category.MISC);
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
        if (!event.isIncoming() && !includeOutgoing.isEnabled()) return;

        String line;
        if (timestamp.isEnabled()) {
            line = "[" + LocalTime.now().format(TIME_FMT) + "] "
                    + (event.isIncoming() ? "[IN] " : "[OUT] ")
                    + event.getMessage();
        } else {
            line = (event.isIncoming() ? "[IN] " : "[OUT] ") + event.getMessage();
        }

        try {
            writer.write(line + System.lineSeparator());
            writer.flush();
        } catch (IOException ignored) {}
    }
}
