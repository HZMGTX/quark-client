package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleLogger extends Module {
    private final BoolSetting logToFile = register(new BoolSetting("Log File", "Write to staff-log.txt", true));
    private final BoolSetting logOutgoing = register(new BoolSetting("Outgoing", "Also log outgoing messages", false));

    public ConsoleLogger() { super("ConsoleLogger", "Logs all chat messages to file for staff review", Category.STAFF); }

    @EventHandler
    public void onChat(EventChat e) {
        if (!e.isIncoming() && !logOutgoing.isEnabled()) return;
        String dir = e.isIncoming() ? "IN" : "OUT";
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String entry = "[" + time + "][" + dir + "] " + e.getMessage();
        if (logToFile.isEnabled()) {
            try (var fw = new FileWriter("staff-log.txt", true)) {
                fw.write(entry + "\n");
            } catch (Exception ignored) {}
        }
    }
}
