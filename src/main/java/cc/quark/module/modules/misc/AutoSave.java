package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AutoSave extends Module {

    private final IntSetting intervalMinutes = register(new IntSetting(
            "Interval", "Minutes between each save", 5, 1, 60));

    private final BoolSetting includeHealth = register(new BoolSetting(
            "SaveHealth", "Include player health in the log", true));

    private final BoolSetting includeInventory = register(new BoolSetting(
            "SaveInventory", "Include hotbar item names in the log", false));

    private final BoolSetting notifyOnSave = register(new BoolSetting(
            "Notify", "Show a chat notification when saving", true));

    private final TimerUtil timer = new TimerUtil();
    private static final String LOG_FILE = "quark_autosave.log";

    public AutoSave() {
        super("AutoSave", "Periodically saves player coordinates and stats to a log file", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(intervalMinutes.get() * 60_000L)) return;

        saveToFile();
        timer.reset();
    }

    private void saveToFile() {
        if (mc.player == null) return;

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        BlockPos pos = mc.player.getBlockPos();

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp).append("] ");
        sb.append("Coords: ").append(pos.getX()).append(", ").append(pos.getY()).append(", ").append(pos.getZ());

        if (includeHealth.isEnabled()) {
            sb.append(" | HP: ").append(String.format("%.1f", mc.player.getHealth()));
        }

        if (mc.getCurrentServerEntry() != null) {
            sb.append(" | Server: ").append(mc.getCurrentServerEntry().address);
        }

        if (mc.world.getRegistryKey() != null) {
            sb.append(" | Dim: ").append(mc.world.getRegistryKey().getValue().getPath());
        }

        if (includeInventory.isEnabled()) {
            sb.append(" | Hotbar: [");
            for (int i = 0; i < 9; i++) {
                var stack = mc.player.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    sb.append(stack.getItem().toString());
                    if (i < 8) sb.append(", ");
                }
            }
            sb.append("]");
        }

        String logLine = sb.toString();

        try {
            File runDir = mc.runDirectory;
            File logFile = new File(runDir, LOG_FILE);
            try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, true))) {
                pw.println(logLine);
            }
            if (notifyOnSave.isEnabled()) {
                ChatUtil.info("AutoSave: Saved to " + logFile.getName());
            }
        } catch (IOException e) {
            ChatUtil.warn("AutoSave: Failed to write log — " + e.getMessage());
        }
    }
}
