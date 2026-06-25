package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.client.MinecraftClient;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ChatLogger2 - Enhanced chat logger with timestamps, keyword filtering, and
 * separate incoming/outgoing log files.
 */
public class ChatLogger2 extends Module {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BoolSetting logIncoming  = register(new BoolSetting("Log Incoming",  "Log messages from the server",     true));
    private final BoolSetting logOutgoing  = register(new BoolSetting("Log Outgoing",  "Log messages sent by the player",  false));
    private final BoolSetting timestamps   = register(new BoolSetting("Timestamps",    "Prepend timestamp to each entry",  true));
    private final BoolSetting filterEnabled = register(new BoolSetting("Filter",       "Only log lines containing keyword",false));
    private final StringSetting keyword    = register(new StringSetting("Keyword",     "Keyword to filter for (case-insensitive)", ""));
    private final BoolSetting splitFiles   = register(new BoolSetting("Split Files",   "Use separate files for IN and OUT", false));
    private final BoolSetting stripColor   = register(new BoolSetting("Strip Color",   "Remove Minecraft color codes",     true));

    private FileWriter inWriter;
    private FileWriter outWriter;

    public ChatLogger2() {
        super("ChatLogger2", "Enhanced chat logger with timestamps, filtering and split-file support", Category.MISC);
    }

    @Override
    public void onEnable() {
        try {
            Path logDir = MinecraftClient.getInstance().runDirectory.toPath().resolve("quark");
            Files.createDirectories(logDir);

            inWriter = new FileWriter(logDir.resolve("chat_in.log").toFile(), true);
            if (splitFiles.isEnabled()) {
                outWriter = new FileWriter(logDir.resolve("chat_out.log").toFile(), true);
            } else {
                outWriter = inWriter; // both go to same file
            }
        } catch (IOException e) {
            inWriter  = null;
            outWriter = null;
        }
    }

    @Override
    public void onDisable() {
        closeWriter(inWriter);
        if (outWriter != inWriter) closeWriter(outWriter);
        inWriter  = null;
        outWriter = null;
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming()  && !logIncoming.isEnabled())  return;
        if (!event.isIncoming() && !logOutgoing.isEnabled())  return;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;

        if (stripColor.isEnabled()) {
            msg = stripColorCodes(msg);
        }

        if (filterEnabled.isEnabled()) {
            String kw = keyword.get().trim().toLowerCase();
            if (!kw.isEmpty() && !msg.toLowerCase().contains(kw)) return;
        }

        String direction = event.isIncoming() ? "[IN] " : "[OUT]";
        String line = timestamps.isEnabled()
                ? "[" + LocalDateTime.now().format(TIME_FMT) + "] " + direction + " " + msg
                : direction + " " + msg;

        FileWriter target = event.isIncoming() ? inWriter : outWriter;
        if (target == null) return;

        try {
            target.write(line + System.lineSeparator());
            target.flush();
        } catch (IOException ignored) {}
    }

    private void closeWriter(FileWriter w) {
        if (w == null) return;
        try { w.close(); } catch (IOException ignored) {}
    }

    private String stripColorCodes(String input) {
        // Remove § + following character (Minecraft formatting codes)
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
    }
}
