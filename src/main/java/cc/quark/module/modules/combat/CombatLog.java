package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * CombatLog — logs damage taken to a file and optionally sends a chat notification.
 */
public class CombatLog extends Module {

    private final BoolSetting chatNotify = register(new BoolSetting(
            "ChatNotify", "Send a chat message when taking damage", true));

    private final BoolSetting logToFile = register(new BoolSetting(
            "LogToFile", "Write damage events to damage_log.txt", true));

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public CombatLog() {
        super("CombatLog", "Logs damage taken to file and/or chat", Category.COMBAT);
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;

        float amount = event.getAmount();
        DamageSource source = event.getSource();
        String sourceName = source.getName();
        String timestamp = DATE_FORMAT.format(new Date());

        String logLine = "[" + timestamp + "] Took " + String.format("%.2f", amount)
                + " damage from " + sourceName;

        if (logToFile.isEnabled()) {
            writeToFile(logLine);
        }

        if (chatNotify.isEnabled() && mc.inGameHud != null) {
            mc.inGameHud.getChatHud().addMessage(
                    Text.literal("§c[CombatLog] §f" + logLine));
        }
    }

    private void writeToFile(String line) {
        try {
            File logFile = new File(mc.runDirectory, "damage_log.txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println(line);
            }
        } catch (IOException e) {
            // Silently ignore file IO errors
        }
    }
}
