package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventBlockBreak;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * BlockLogger - Logs block break events observed client-side to a local file
 * and optionally warns when a suspicious number of breaks occur quickly.
 */
public class BlockLogger extends Module {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final BoolSetting saveToFile = register(new BoolSetting(
            "Save To File", "Persist log entries to quark/block_log.txt", true));

    private final BoolSetting printToChat = register(new BoolSetting(
            "Print To Chat", "Print block break events locally", false));

    private final BoolSetting alertRapid = register(new BoolSetting(
            "Alert Rapid", "Warn when many breaks happen in a short window", true));

    private final IntSetting rapidThreshold = register(new IntSetting(
            "Rapid Threshold", "Breaks per second to trigger rapid-break alert", 8, 2, 30));

    private final BoolSetting logPlayerOwn = register(new BoolSetting(
            "Log Own Breaks", "Include the local player's own block breaks", false));

    // Ring buffer for rate detection
    private final List<Long> recentBreakTimes = new ArrayList<>();
    private FileWriter writer;

    public BlockLogger() {
        super("BlockLogger", "Logs block place/break events observed by the client", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        try {
            Path dir = MinecraftClient.getInstance().runDirectory.toPath().resolve("quark");
            Files.createDirectories(dir);
            writer = new FileWriter(dir.resolve("block_log.txt").toFile(), true);
        } catch (IOException e) {
            writer = null;
            ChatUtil.error("[BlockLogger] Could not open log file: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (writer != null) {
            try { writer.close(); } catch (IOException ignored) {}
            writer = null;
        }
        recentBreakTimes.clear();
    }

    @EventHandler
    public void onBlockBreak(EventBlockBreak event) {
        if (mc.player == null) return;

        BlockPos pos = event.getPos();
        String blockName = event.getState().getBlock().getName().getString();

        long now = System.currentTimeMillis();
        recentBreakTimes.add(now);
        // Keep only last 5 seconds
        recentBreakTimes.removeIf(t -> now - t > 5000);

        // Rate check
        if (alertRapid.isEnabled()) {
            double breaksPerSec = recentBreakTimes.size() / 5.0;
            if (breaksPerSec >= rapidThreshold.get()) {
                ChatUtil.warn("[BlockLogger] Rapid block breaking detected! ("
                        + String.format("%.1f", breaksPerSec) + " breaks/s)");
            }
        }

        String entry = "[" + FMT.format(LocalDateTime.now()) + "] BREAK "
                + blockName + " @ " + pos.getX() + "," + pos.getY() + "," + pos.getZ();

        if (printToChat.isEnabled()) {
            ChatUtil.info(entry);
        }

        if (saveToFile.isEnabled() && writer != null) {
            try {
                writer.write(entry + System.lineSeparator());
                writer.flush();
            } catch (IOException ignored) {}
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        // Periodic flush guard
        if (writer != null) {
            try { writer.flush(); } catch (IOException ignored) {}
        }
    }
}
