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

public class CommandSpy2 extends Module {

    private final BoolSetting showInChat = register(new BoolSetting(
            "Show In Chat", "Display intercepted commands in local chat", true));
    private final BoolSetting logToFile = register(new BoolSetting(
            "Log To File", "Write commands to commandspy.log", true));
    private final StringSetting filterPrefix = register(new StringSetting(
            "Filter Prefix", "Only log commands starting with this prefix (empty = all)", ""));
    private final BoolSetting includeOwn = register(new BoolSetting(
            "Include Own", "Also log your own outgoing commands", false));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public CommandSpy2() {
        super("CommandSpy2", "Logs all commands used by players to chat", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        ChatUtil.info("§6[CommandSpy2] §fNow logging player commands.");
    }

    @EventHandler
    public void onChat(EventChat event) {
        String msg = event.getMessage();
        if (msg == null) return;

        // Strip color codes
        String clean = msg.replaceAll("§[0-9a-fklmnorA-FK-OR]", "").trim();

        boolean isIncoming = event.isIncoming();
        boolean isOutgoing = !isIncoming;

        // Skip own outgoing commands if the setting says so
        if (isOutgoing && !includeOwn.isEnabled()) return;

        // Detect command pattern: starts with '/' or is a command echo from server
        boolean isCommand = false;
        String detectedCmd = null;

        if (isOutgoing && clean.startsWith("/")) {
            isCommand = true;
            detectedCmd = clean;
        } else if (isIncoming) {
            // Typical server command-spy echo: "[PlayerName: /command]" or "PlayerName issued command: /command"
            if (clean.contains(": /") || clean.matches(".*issued server command:.*")) {
                isCommand = true;
                detectedCmd = clean;
            }
        }

        if (!isCommand || detectedCmd == null) return;

        // Apply prefix filter
        String prefix = filterPrefix.get().trim();
        if (!prefix.isEmpty()) {
            // Find the slash portion
            int slashIdx = detectedCmd.indexOf('/');
            String cmdPart = slashIdx >= 0 ? detectedCmd.substring(slashIdx + 1) : detectedCmd;
            if (!cmdPart.toLowerCase().startsWith(prefix.toLowerCase())) return;
        }

        String timestamp = LocalDateTime.now().format(FMT);
        String entry = "[" + timestamp + "] " + (isOutgoing ? "[YOU] " : "") + detectedCmd;

        if (showInChat.isEnabled()) {
            ChatUtil.info("§6[CommandSpy2] §f" + (isOutgoing ? "§7[YOU] " : "") + "§e" + detectedCmd);
        }
        if (logToFile.isEnabled()) {
            writeLog(entry);
        }
    }

    private void writeLog(String line) {
        try {
            Path dir = mc.runDirectory.toPath().resolve("quark");
            Files.createDirectories(dir);
            try (PrintWriter pw = new PrintWriter(new FileWriter(dir.resolve("commandspy.log").toFile(), true))) {
                pw.println(line);
            }
        } catch (IOException ignored) {}
    }
}
