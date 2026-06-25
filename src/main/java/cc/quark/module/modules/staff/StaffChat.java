package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StaffChat extends Module {

    private final BoolSetting logToFile = register(new BoolSetting(
            "Log To File", "Save flagged messages to .minecraft/quark/staffchat.log", true));

    private final BoolSetting alertOnKeyword = register(new BoolSetting(
            "Alert On Keyword", "Highlight messages that contain staff-related keywords", true));

    private final StringSetting keywords = register(new StringSetting(
            "Keywords", "Comma-separated keywords to watch for", "staff,admin,mod,ban,mute,kick,freeze,vanish"));

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public StaffChat() {
        super("StaffChat", "Chat securely with other staff members.", Category.STAFF, 0);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;

        String message = event.getMessage();
        if (message == null || message.isEmpty()) return;

        String lowerMessage = message.toLowerCase();

        // Parse keyword list
        String[] kwArray = keywords.get().split(",");
        boolean matched = false;
        String matchedKeyword = null;

        for (String kw : kwArray) {
            String trimmed = kw.trim().toLowerCase();
            if (!trimmed.isEmpty() && lowerMessage.contains(trimmed)) {
                matched = true;
                matchedKeyword = trimmed;
                break;
            }
        }

        if (!matched) return;

        if (alertOnKeyword.isEnabled()) {
            // Print a highlighted alert into chat
            String alert = Formatting.RED + "[StaffChat] " + Formatting.YELLOW
                    + "Keyword \"" + matchedKeyword + "\" detected: "
                    + Formatting.WHITE + message;
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal(alert), false);
            }
        }

        if (logToFile.isEnabled()) {
            writeToLog(message);
        }
    }

    private void writeToLog(String message) {
        try {
            File gameDir = MinecraftClient.getInstance().runDirectory;
            File logDir  = new File(gameDir, "quark");
            if (!logDir.exists()) logDir.mkdirs();

            File logFile = new File(logDir, "staffchat.log");
            try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                pw.println("[" + DATE_FORMAT.format(new Date()) + "] " + message);
            }
        } catch (IOException e) {
            // Silently ignore file I/O errors to avoid spamming the client
        }
    }
}
