package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;

public class AntiCheatMonitor extends Module {

    private final BoolSetting showHud = register(new BoolSetting(
            "Show HUD", "Display flag events as a HUD overlay (placeholder)", false));
    private final BoolSetting showChat = register(new BoolSetting(
            "Show Chat", "Print new flag events to local staff chat", true));
    private final IntSetting maxEntries = register(new IntSetting(
            "Max Entries", "Maximum recent flag entries to retain", 10, 5, 20));

    // Shared static event log; accessed by other detector modules via logEvent()
    private static final Deque<FlagEntry> eventLog = new ArrayDeque<>();
    private static final Object LOG_LOCK = new Object();
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm:ss");

    // Reference to active instance so static logEvent can reach settings
    private static AntiCheatMonitor instance;

    private int lastLogSize = 0;
    private int tickCounter = 0;

    public AntiCheatMonitor() {
        super("AntiCheatMonitor", "Live overlay of all recent flag events from all active detectors", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        instance = this;
        synchronized (LOG_LOCK) { eventLog.clear(); }
        lastLogSize = 0;
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[AntiCheatMonitor] §fLive flag log active (max §e" + maxEntries.get() + "§f entries).");
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

        // Every 2 seconds dump new events to chat if showChat is enabled
        if (tickCounter % 40 != 0) return;
        synchronized (LOG_LOCK) {
            int size = eventLog.size();
            if (size > lastLogSize && showChat.isEnabled()) {
                // Print the entries added since last dump
                int skip = lastLogSize;
                int idx = 0;
                for (FlagEntry e : eventLog) {
                    if (idx >= skip) {
                        ChatUtil.warn("§8[ACM §7" + e.time + "§8] §e" + e.player
                                + " §7— §c" + e.detector + " §f" + e.detail);
                    }
                    idx++;
                }
            }
            lastLogSize = size;
        }
    }

    /**
     * Called by other detector modules to log a flag event.
     */
    public static void logEvent(String player, String detector, String detail) {
        synchronized (LOG_LOCK) {
            int max = instance != null ? instance.maxEntries.get() : 10;
            while (eventLog.size() >= max) eventLog.pollFirst();
            eventLog.addLast(new FlagEntry(player, detector, detail, TIME_FMT.format(new Date())));
        }
    }

    /**
     * Returns a snapshot of the current event log for external display.
     */
    public static Deque<FlagEntry> getLog() {
        synchronized (LOG_LOCK) {
            return new ArrayDeque<>(eventLog);
        }
    }

    public static class FlagEntry {
        public final String player;
        public final String detector;
        public final String detail;
        public final String time;

        public FlagEntry(String player, String detector, String detail, String time) {
            this.player = player;
            this.detector = detector;
            this.detail = detail;
            this.time = time;
        }
    }
}
