package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMonitor extends Module {
    private final StringSetting patterns = register(new StringSetting("Patterns", "Comma-separated words/regex to flag", "cheat,hack,x-ray,fly,esp,kill aura,killaura"));
    private final BoolSetting logAll = register(new BoolSetting("Log All", "Log all chat to chatlog.txt", false));
    private final BoolSetting alertOnFlag = register(new BoolSetting("Alert On Flag", "Flash alert when pattern matched", true));
    private final BoolSetting autoReport = register(new BoolSetting("Auto Report", "Send /report on keyword match", false));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ChatMonitor() {
        super("Chat Monitor", "Monitor chat for suspicious keywords with logging", Category.STAFF, 0);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg == null) return;
        String clean = msg.replaceAll("§[0-9a-fklmnorA-FK-OR]", "").trim();

        if (logAll.isEnabled()) writeLog("[CHAT] " + clean);

        String lower = clean.toLowerCase();
        for (String pattern : patterns.get().split(",")) {
            pattern = pattern.trim().toLowerCase();
            if (pattern.isEmpty()) continue;
            if (lower.contains(pattern)) {
                String alert = "§c[ChatMonitor] §fFlagged: §e" + clean.substring(0, Math.min(clean.length(), 60));
                if (alertOnFlag.isEnabled()) ChatUtil.warn(alert);
                writeLog("[FLAGGED:" + pattern + "] " + clean);

                String playerName = extractName(clean);
                if (autoReport.isEnabled() && playerName != null && mc.player != null) {
                    mc.player.networkHandler.sendChatCommand("report " + playerName + " " + pattern);
                }
                break;
            }
        }
    }

    private String extractName(String msg) {
        if (msg.startsWith("<") && msg.contains(">")) {
            return msg.substring(1, msg.indexOf('>'));
        }
        if (msg.contains(": ") && msg.indexOf(": ") < 20) {
            return msg.substring(0, msg.indexOf(": "));
        }
        return null;
    }

    private void writeLog(String line) {
        try {
            Path dir = mc.runDirectory.toPath().resolve("quark");
            Files.createDirectories(dir);
            try (PrintWriter pw = new PrintWriter(new FileWriter(dir.resolve("chatmonitor.log").toFile(), true))) {
                pw.println("[" + LocalDateTime.now().format(FMT) + "] " + line);
            }
        } catch (IOException ignored) {}
    }
}
