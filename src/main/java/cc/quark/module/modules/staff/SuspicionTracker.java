package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SuspicionTracker extends Module {

    private final ModeSetting displayMode = register(new ModeSetting(
            "Display Mode", "Where to display suspicion info", "Chat", "Chat", "HUD", "Both"));
    private final BoolSetting decay = register(new BoolSetting(
            "Decay", "Gradually reduce suspicion scores over time when module is active", true));

    // Shared static score map used by all detector modules
    private static final Map<UUID, PlayerSuspicion> scores = new HashMap<>();
    private static final Object LOCK = new Object();

    // Active instance reference for decay tick access
    private static SuspicionTracker instance;

    private int tickCounter = 0;
    // Decay every 30 seconds (600 ticks)
    private static final int DECAY_INTERVAL_TICKS = 600;
    private static final int DECAY_AMOUNT = 5;

    public SuspicionTracker() {
        super("SuspicionTracker", "Tracks cumulative suspicion scores per player across all detectors", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        instance = this;
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[SuspicionTracker] §fTracking suspicion scores. Decay=§e" + decay.isEnabled()
                + "§f, display=§e" + displayMode.get() + "§f.");
        printTop5();
    }

    @Override
    public void onDisable() {
        instance = null;
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;
        tickCounter++;

        if (decay.isEnabled() && tickCounter % DECAY_INTERVAL_TICKS == 0) {
            synchronized (LOCK) {
                scores.values().forEach(ps -> ps.score = Math.max(0, ps.score - DECAY_AMOUNT));
                scores.entrySet().removeIf(e -> e.getValue().score == 0);
            }
        }

        // Print summary every 5 minutes to chat / HUD
        if (tickCounter % 6000 == 0 && !scores.isEmpty()) {
            if ("Chat".equals(displayMode.get()) || "Both".equals(displayMode.get())) {
                printTop5();
            }
        }
    }

    /**
     * Called by other detector modules to increment a player's suspicion score.
     */
    public static void addSuspicion(UUID id, String name, int amount, String source) {
        synchronized (LOCK) {
            PlayerSuspicion ps = scores.computeIfAbsent(id, k -> new PlayerSuspicion(name));
            ps.name = name; // keep name fresh
            ps.score += amount;
            ps.lastSource = source;
        }
        AntiCheatMonitor.logEvent(name, source, "+" + amount + " pts → total " + getScore(id));
    }

    public static int getScore(UUID id) {
        synchronized (LOCK) {
            PlayerSuspicion ps = scores.get(id);
            return ps != null ? ps.score : 0;
        }
    }

    public static Map<UUID, PlayerSuspicion> getScores() {
        synchronized (LOCK) {
            return Collections.unmodifiableMap(scores);
        }
    }

    private void printTop5() {
        if (scores.isEmpty()) {
            ChatUtil.info("§6[SuspicionTracker] §fNo suspicion data recorded yet.");
            return;
        }
        ChatUtil.info("§6[SuspicionTracker] §fTop suspects:");
        scores.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue().score, a.getValue().score))
                .limit(5)
                .forEach(e -> ChatUtil.warn("  §e" + e.getValue().name
                        + " §f— §c" + e.getValue().score + " §fpts §7(last: " + e.getValue().lastSource + ")"));
    }

    public static class PlayerSuspicion {
        public String name;
        public int score;
        public String lastSource;

        public PlayerSuspicion(String name) {
            this.name = name;
            this.score = 0;
            this.lastSource = "none";
        }
    }
}
