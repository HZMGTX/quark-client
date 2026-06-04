package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CrashLogger extends Module {

    private final BoolSetting enabled = register(new BoolSetting(
            "Enabled", "Enable crash context logging", true));

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long LOG_INTERVAL = 5000L;
    private long lastLog = 0;

    // Thread for detecting game hangs
    private Thread watchdog;
    private volatile long lastTickTime = System.currentTimeMillis();

    public CrashLogger() {
        super("CrashLogger", "Logs game crashes with context", Category.MISC);
    }

    @Override
    public void onEnable() {
        lastTickTime = System.currentTimeMillis();
        startWatchdog();
    }

    @Override
    public void onDisable() {
        stopWatchdog();
    }

    private void startWatchdog() {
        watchdog = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000);
                    long sinceLastTick = System.currentTimeMillis() - lastTickTime;
                    if (sinceLastTick > 10000) {
                        // Game may have frozen - log context
                        logCrashContext("WATCHDOG: Game tick frozen for " + sinceLastTick + "ms");
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "CrashLogger-Watchdog");
        watchdog.setDaemon(true);
        watchdog.start();
    }

    private void stopWatchdog() {
        if (watchdog != null) {
            watchdog.interrupt();
            watchdog = null;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        lastTickTime = System.currentTimeMillis();
        if (!enabled.isEnabled()) return;
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastLog < LOG_INTERVAL) return;
        lastLog = now;

        // Write a periodic context snapshot
        logCrashContext(buildContext());
    }

    private String buildContext() {
        if (mc.player == null) return "player=null";
        Vec3d pos = mc.player.getPos();
        float health = mc.player.getHealth();
        int entities = mc.world != null ? (int) mc.world.getEntities().spliterator().estimateSize() : -1;

        return String.format("pos=%.1f,%.1f,%.1f hp=%.1f entities=%d server=%s",
                pos.x, pos.y, pos.z, health, entities, getCurrentServer());
    }

    private String getCurrentServer() {
        if (mc.getCurrentServerEntry() != null) return mc.getCurrentServerEntry().address;
        return "singleplayer";
    }

    private void logCrashContext(String context) {
        try {
            Path file = mc.runDirectory.toPath().resolve("quark").resolve("crash_context.log");
            Files.createDirectories(file.getParent());
            try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                fw.write("[" + LocalDateTime.now().format(FMT) + "] " + context + System.lineSeparator());
            }
        } catch (IOException ignored) {}
    }
}
