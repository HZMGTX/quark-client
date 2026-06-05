package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportHandler extends Module {
    private final BoolSetting autoAck = register(new BoolSetting("Auto Ack", "Auto-reply to reports", false));
    private final BoolSetting logReports = register(new BoolSetting("Log Reports", "Save reports to reports.log", true));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportHandler() {
        super("Report Handler", "Catch and log player reports for review", Category.STAFF, 0);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg == null) return;
        String clean = msg.replaceAll("§[0-9a-fklmnorA-FK-OR]", "").trim().toLowerCase();

        boolean isReport = clean.contains("report") || clean.contains("reported") ||
                           clean.contains("cheating") || clean.contains("hacking");
        if (!isReport) return;

        String original = msg.replaceAll("§[0-9a-fklmnorA-FK-OR]", "").trim();
        ChatUtil.warn("§c[Report] §f" + original);

        if (logReports.isEnabled()) {
            try {
                Path dir = mc.runDirectory.toPath().resolve("quark");
                Files.createDirectories(dir);
                try (PrintWriter pw = new PrintWriter(new FileWriter(dir.resolve("reports.log").toFile(), true))) {
                    pw.println("[" + LocalDateTime.now().format(FMT) + "] " + original);
                }
            } catch (IOException ignored) {}
        }
    }
}
