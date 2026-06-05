package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class WatchList extends Module {
    private final StringSetting watched = register(new StringSetting("Players", "Comma-separated names to watch", ""));
    private final BoolSetting alertMovement = register(new BoolSetting("Movement Alert", "Alert on suspicious speed", true));
    private final BoolSetting alertCombat = register(new BoolSetting("Combat Alert", "Alert on fast attack patterns", false));
    private final BoolSetting logPositions = register(new BoolSetting("Log Positions", "Log positions to watchlist.log", false));

    private final Map<String, Vec3d> lastPositions = new HashMap<>();
    private final TimerUtil timer = new TimerUtil();

    public WatchList() {
        super("Watch List", "Monitor flagged players for suspicious behavior", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        lastPositions.clear();
        ChatUtil.info("[WatchList] Monitoring: " + watched.get());
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        Set<String> targets = getWatched();
        if (targets.isEmpty()) return;

        for (var entity : mc.world.getPlayers()) {
            if (!(entity instanceof PlayerEntity p)) continue;
            String name = p.getName().getString();
            if (!targets.contains(name)) continue;

            Vec3d lastPos = lastPositions.get(name);
            Vec3d currentPos = p.getPos();

            if (lastPos != null && alertMovement.isEnabled()) {
                double dist = currentPos.distanceTo(lastPos);
                double speed = dist / 0.5; // blocks per second (checked every 500ms)
                if (speed > 8.0) {
                    ChatUtil.warn("[WatchList] §c" + name + " §fis moving suspiciously fast: §c" + String.format("%.1f", speed) + " bps");
                }
            }

            lastPositions.put(name, currentPos);

            if (logPositions.isEnabled()) {
                writeLog(name + " @ " + (int) currentPos.x + "," + (int) currentPos.y + "," + (int) currentPos.z);
            }
        }
    }

    private Set<String> getWatched() {
        String s = watched.get().trim();
        if (s.isEmpty()) return new HashSet<>();
        return new HashSet<>(Arrays.asList(s.split(",")));
    }

    private void writeLog(String line) {
        try {
            java.nio.file.Path dir = mc.runDirectory.toPath().resolve("quark");
            java.nio.file.Files.createDirectories(dir);
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(dir.resolve("watchlist.log").toFile(), true))) {
                pw.println("[" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + line);
            }
        } catch (java.io.IOException ignored) {}
    }
}
