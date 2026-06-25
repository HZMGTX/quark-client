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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StaffChatLogger extends Module {

    private final BoolSetting logCommands = register(new BoolSetting(
            "Log Commands", "Include messages starting with '/' (commands)", true));
    private final BoolSetting logPrivate = register(new BoolSetting(
            "Log Private", "Include messages that appear to be whispers/tells", true));
    private final BoolSetting toFile = register(new BoolSetting(
            "To File", "Write log entries to chat_log.txt alongside the Minecraft directory", false));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private PrintWriter fileWriter = null;

    public StaffChatLogger() {
        super("StaffChatLogger", "Logs all incoming chat messages and commands with timestamps", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (toFile.isEnabled()) {
            try {
                fileWriter = new PrintWriter(new FileWriter("chat_log.txt", true), true);
                ChatUtil.success("§6[ChatLogger] §fLogging to §echat_log.txt");
            } catch (IOException e) {
                ChatUtil.error("[ChatLogger] Could not open chat_log.txt: " + e.getMessage());
                fileWriter = null;
            }
        }
        ChatUtil.info("§6[ChatLogger] §fChat logging started.");
    }

    @Override
    public void onDisable() {
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
        ChatUtil.info("§6[ChatLogger] §fChat logging stopped.");
    }

    @EventHandler
    public void onChat(EventChat event) {
        String msg = event.getMessage();
        if (msg == null || msg.isBlank()) return;

        // Filter commands
        boolean isCommand = msg.startsWith("/");
        if (isCommand && !logCommands.isEnabled()) return;

        // Heuristic for private messages (common plugin formats: /msg, /tell, /w, /r)
        boolean isPrivate = isCommand && (msg.startsWith("/msg ") || msg.startsWith("/tell ")
                || msg.startsWith("/w ") || msg.startsWith("/r "));
        if (isPrivate && !logPrivate.isEnabled()) return;

        String direction = event.isIncoming() ? "IN " : "OUT";
        String timestamp = LocalDateTime.now().format(FMT);
        String entry = "[" + timestamp + "] [" + direction + "] " + msg;

        // Always log to console/local chat as an info line
        ChatUtil.info("§8[Log] §7" + msg);

        if (toFile.isEnabled() && fileWriter != null) {
            fileWriter.println(entry);
        }
    }
}
