package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Spy extends Module {

    private final BoolSetting logToFile = register(new BoolSetting(
            "Log To File", "Write all monitored messages to spy.log", true));
    private final BoolSetting filterPrivate = register(new BoolSetting(
            "Private Messages", "Intercept /msg, /tell, /w patterns", true));
    private final BoolSetting filterCommands = register(new BoolSetting(
            "Log Commands", "Log /command patterns from chat", false));
    private final StringSetting keywords = register(new StringSetting(
            "Keywords", "Comma-separated words to highlight (case-insensitive)", "ban,kick,mute,ip,address,vpn"));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Spy() {
        super("Spy", "Monitors and logs chat messages matching staff keywords or PM patterns", Category.STAFF, 0);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg == null) return;

        String clean = msg.replaceAll("§[0-9a-fklmnor]", "").trim();
        boolean shouldLog = false;

        if (filterPrivate.isEnabled()) {
            // Look for common private-message patterns: [name -> you] or /msg etc.
            if (clean.matches(".*\\[.*->.*].*") || clean.toLowerCase().startsWith("msg ")
                    || clean.toLowerCase().startsWith("tell ")) {
                shouldLog = true;
            }
        }

        if (!shouldLog && filterCommands.isEnabled() && clean.startsWith("/")) {
            shouldLog = true;
        }

        if (!shouldLog) {
            String lower = clean.toLowerCase();
            for (String kw : keywords.get().split(",")) {
                if (lower.contains(kw.trim())) { shouldLog = true; break; }
            }
        }

        if (shouldLog) {
            String entry = "[" + LocalDateTime.now().format(FMT) + "] " + clean;
            if (logToFile.isEnabled()) writeToLog(entry);
        }
    }

    private void writeToLog(String line) {
        try {
            Path logDir = mc.runDirectory.toPath().resolve("quark");
            Files.createDirectories(logDir);
            try (PrintWriter pw = new PrintWriter(new FileWriter(logDir.resolve("spy.log").toFile(), true))) {
                pw.println(line);
            }
        } catch (IOException ignored) {}
    }
}
