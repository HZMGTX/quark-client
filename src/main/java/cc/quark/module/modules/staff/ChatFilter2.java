package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatFilter2 extends Module {

    private final StringSetting blocklist = register(new StringSetting(
            "Blocklist", "Comma-separated words to suppress from chat", ""));
    private final StringSetting alertlist = register(new StringSetting(
            "Alert List", "Comma-separated words to flag with a notification", "admin,cheat,hack,ban,mute"));
    private final BoolSetting suppressFiltered = register(new BoolSetting(
            "Suppress Filtered", "Hide blocklist messages from local chat", true));
    private final BoolSetting logFiltered = register(new BoolSetting(
            "Log Filtered", "Write filtered messages to a log file", false));
    private final BoolSetting highlightAlerts = register(new BoolSetting(
            "Highlight Alerts", "Colour alertlist matches in red", true));

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ChatFilter2() {
        super("ChatFilter2", "Advanced server-side chat filtering with block and alert lists", Category.STAFF, 0);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;
        String lower = msg.toLowerCase();

        // Blocklist check
        for (String word : blocklist.get().split(",")) {
            String w = word.trim().toLowerCase();
            if (!w.isEmpty() && lower.contains(w)) {
                if (suppressFiltered.isEnabled()) {
                    event.cancel();
                }
                if (logFiltered.isEnabled()) writeToLog("[BLOCKED] " + msg);
                return;
            }
        }

        // Alert list check
        if (highlightAlerts.isEnabled()) {
            for (String word : alertlist.get().split(",")) {
                String w = word.trim().toLowerCase();
                if (!w.isEmpty() && lower.contains(w)) {
                    if (mc.player != null) {
                        String alert = Formatting.RED + "[ChatFilter2] " + Formatting.YELLOW
                                + "Alert keyword \"" + w + "\": " + Formatting.WHITE + msg;
                        mc.player.sendMessage(Text.literal(alert), false);
                    }
                    if (logFiltered.isEnabled()) writeToLog("[ALERT] " + msg);
                    break;
                }
            }
        }
    }

    private void writeToLog(String line) {
        try {
            File dir = new File(MinecraftClient.getInstance().runDirectory, "quark");
            if (!dir.exists()) dir.mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(new File(dir, "chatfilter2.log"), true))) {
                pw.println("[" + DATE_FORMAT.format(new Date()) + "] " + line);
            }
        } catch (IOException ignored) {}
    }
}
