package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AimAssistDetector extends Module {

    private final DoubleSetting snapThreshold = register(new DoubleSetting(
            "Snap Threshold", "Degrees/tick rotation snap that triggers a flag", 90.0, 45.0, 180.0));
    private final BoolSetting logToChat = register(new BoolSetting(
            "Log To Chat", "Print each flag event to local staff chat", true));

    // UUID -> last yaw
    private final Map<UUID, Float> lastYaw = new HashMap<>();
    // UUID -> last pitch
    private final Map<UUID, Float> lastPitch = new HashMap<>();
    // UUID -> recent rotation deltas (for variance analysis)
    private final Map<UUID, List<Double>> rotHistory = new HashMap<>();
    // UUID -> flag count
    private final Map<UUID, Integer> flags = new HashMap<>();
    // UUID -> last alert time
    private final Map<UUID, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 7_000;
    private static final int HISTORY_SIZE = 20;
    private static final int FLAG_THRESHOLD = 4;

    public AimAssistDetector() {
        super("AimAssistDetector", "Detects unnatural aim snap and suspiciously perfect target tracking", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        lastYaw.clear();
        lastPitch.clear();
        rotHistory.clear();
        flags.clear();
        lastAlert.clear();
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[AimAssistDetector] §fTracking rotation patterns (snap > §e" + snapThreshold.get() + "°§f).");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        lastYaw.clear();
        lastPitch.clear();
        rotHistory.clear();
        flags.clear();
        lastAlert.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            UUID id = player.getUuid();
            String name = player.getName().getString();

            float yaw = player.getYaw();
            float pitch = player.getPitch();
            Float prevYaw = lastYaw.get(id);
            Float prevPitch = lastPitch.get(id);

            if (prevYaw != null && prevPitch != null) {
                double dy = Math.abs(yaw - prevYaw) % 360.0;
                if (dy > 180.0) dy = 360.0 - dy;
                double dp = Math.abs(pitch - prevPitch);
                double totalDelta = Math.sqrt(dy * dy + dp * dp);

                // Record delta into history
                List<Double> hist = rotHistory.computeIfAbsent(id, k -> new ArrayList<>());
                hist.add(totalDelta);
                if (hist.size() > HISTORY_SIZE) hist.remove(0);

                // Snap detection: single tick with extreme delta while attacking
                if (totalDelta > snapThreshold.get() && player.handSwinging) {
                    int f = flags.getOrDefault(id, 0) + 1;
                    flags.put(id, f);

                    if (f >= FLAG_THRESHOLD) {
                        triggerAlert(id, name, String.format("rotation snap §c%.1f°§f while attacking", totalDelta));
                        flags.put(id, 0);
                    }
                }

                // Low-variance tracking: extremely consistent small deltas = aim assist bot
                if (hist.size() == HISTORY_SIZE) {
                    double mean = hist.stream().mapToDouble(d -> d).average().orElse(0);
                    double variance = hist.stream().mapToDouble(d -> (d - mean) * (d - mean)).average().orElse(0);
                    // Suspiciously low variance (<0.5) with constant movement toward another player
                    if (variance < 0.5 && mean > 0.1 && mean < 5.0) {
                        triggerAlert(id, name, String.format("near-zero rotation variance §c%.4f §f(mean §e%.2f°§f)", variance, mean));
                    }
                }
            }

            lastYaw.put(id, yaw);
            lastPitch.put(id, pitch);
        }
    }

    private void triggerAlert(UUID id, String name, String detail) {
        long now = System.currentTimeMillis();
        Long last = lastAlert.get(id);
        if (last != null && now - last < ALERT_COOLDOWN_MS) return;
        lastAlert.put(id, now);
        if (logToChat.isEnabled()) {
            ChatUtil.warn("§c[AimAssistDetector] §e" + name + " §f— " + detail);
        }
        SuspicionTracker.addSuspicion(id, name, 14, "AimAssistDetector");
    }
}
