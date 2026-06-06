package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MacroDetector extends Module {

    private final DoubleSetting variance = register(new DoubleSetting(
            "Variance (ms)", "Max ms variance between actions to flag as macro", 20.0, 5.0, 50.0));
    private final IntSetting minSamples = register(new IntSetting(
            "Min Samples", "Minimum action samples required before analysis", 20, 10, 50));
    private final BoolSetting alertStaff = register(new BoolSetting(
            "Alert Staff", "Notify staff when macro-like pattern is detected", true));

    // UUID -> list of timestamps when the player swung their arm
    private final Map<UUID, List<Long>> swingTimestamps = new HashMap<>();
    // UUID -> last alert time
    private final Map<UUID, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 15_000;

    public MacroDetector() {
        super("MacroDetector", "Detects macro use via statistical analysis of perfectly timed repetitive actions", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        swingTimestamps.clear();
        lastAlert.clear();
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[MacroDetector] §fStatistical macro detection active (variance ≤§e" + variance.get() + "ms§f).");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        swingTimestamps.clear();
        lastAlert.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        long now = System.currentTimeMillis();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            UUID id = player.getUuid();

            // Sample arm swings as a proxy for automated actions
            if (player.handSwinging && player.handSwingTicks == 1) {
                List<Long> times = swingTimestamps.computeIfAbsent(id, k -> new ArrayList<>());
                times.add(now);
                // Keep only the last N samples
                if (times.size() > minSamples.get() + 10) times.remove(0);

                if (times.size() >= minSamples.get()) {
                    analyzePattern(id, player.getName().getString(), times);
                }
            }
        }
    }

    private void analyzePattern(UUID id, String name, List<Long> times) {
        // Compute intervals between consecutive samples
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < times.size(); i++) {
            intervals.add(times.get(i) - times.get(i - 1));
        }

        double mean = intervals.stream().mapToLong(l -> l).average().orElse(0);
        double var = intervals.stream().mapToDouble(l -> (l - mean) * (l - mean)).average().orElse(0);
        double stdDev = Math.sqrt(var);

        if (stdDev <= variance.get() && mean > 30 && mean < 2000) {
            long now = System.currentTimeMillis();
            Long last = lastAlert.get(id);
            if (last != null && now - last < ALERT_COOLDOWN_MS) return;
            lastAlert.put(id, now);

            if (alertStaff.isEnabled()) {
                ChatUtil.warn(String.format(
                        "§c[MacroDetector] §e%s §f— suspiciously regular actions: mean §e%.0fms §fstdDev §c%.2fms §7(threshold §a%.0fms§7)",
                        name, mean, stdDev, variance.get()));
            }
            SuspicionTracker.addSuspicion(id, name, 16, "MacroDetector");
        }
    }
}
