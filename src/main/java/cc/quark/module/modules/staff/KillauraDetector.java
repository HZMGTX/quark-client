package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class KillauraDetector extends Module {

    private final IntSetting maxCPS = register(new IntSetting(
            "Max CPS", "Maximum clicks per second before flagging", 14, 10, 20));
    private final BoolSetting wallCheck = register(new BoolSetting(
            "Wall Check", "Flag attacks made through solid blocks", true));
    private final BoolSetting notifyStaff = register(new BoolSetting(
            "Notify Staff", "Send alert to staff chat on detection", true));

    // UUID -> list of attack timestamps in last second
    private final Map<UUID, List<Long>> attackTimestamps = new HashMap<>();
    // UUID -> last yaw when attacking (to detect 360-degree targeting)
    private final Map<UUID, Float> lastYaw = new HashMap<>();
    // UUID -> count of suspicious 360 snap detections
    private final Map<UUID, Integer> snapFlags = new HashMap<>();
    // UUID -> last alert time
    private final Map<UUID, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 8_000;

    public KillauraDetector() {
        super("KillauraDetector", "Monitors attack CPS, wall-hits, and 360-degree targeting patterns", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        attackTimestamps.clear();
        lastYaw.clear();
        snapFlags.clear();
        lastAlert.clear();
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[KillauraDetector] §fWatching attack patterns (max CPS: §e" + maxCPS.get() + "§f).");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        attackTimestamps.clear();
        lastYaw.clear();
        snapFlags.clear();
        lastAlert.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        long now = System.currentTimeMillis();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            UUID id = player.getUuid();
            String name = player.getName().getString();

            // Simulate CPS detection: track swing arm animation ticks as attack proxy
            if (player.handSwinging && player.handSwingTicks == 1) {
                List<Long> times = attackTimestamps.computeIfAbsent(id, k -> new ArrayList<>());
                times.add(now);
                // Prune old entries outside 1-second window
                times.removeIf(t -> now - t > 1000);

                int cps = times.size();
                if (cps > maxCPS.get()) {
                    flag(id, name, "High CPS (" + cps + "/" + maxCPS.get() + ")", 20);
                }

                // Check yaw snap (killaura snaps to targets in unnatural angles)
                float yaw = player.getYaw();
                Float prevYaw = lastYaw.get(id);
                if (prevYaw != null) {
                    float delta = Math.abs(yaw - prevYaw) % 360f;
                    if (delta > 180f) delta = 360f - delta;
                    if (delta > 120f) {
                        int snaps = snapFlags.getOrDefault(id, 0) + 1;
                        snapFlags.put(id, snaps);
                        if (snaps >= 3) {
                            flag(id, name, "360° targeting snap (" + String.format("%.1f", delta) + "°)", 15);
                            snapFlags.put(id, 0);
                        }
                    }
                }
                lastYaw.put(id, yaw);
            }

            // Wall hit check: player attacking but target behind solid block
            if (wallCheck.isEnabled() && player.handSwinging) {
                Vec3d origin = player.getCameraPosVec(1.0f);
                Vec3d look = player.getRotationVec(1.0f);
                Vec3d end = origin.add(look.multiply(6.0));
                var raycast = mc.world.raycastBlock(origin, end, player.getBlockPos(),
                        player.getBoundingBox().expand(6), s -> !s.isAir());
                if (raycast != null) {
                    flag(id, name, "Possible through-wall attack", 10);
                }
            }
        }
    }

    private void flag(UUID id, String name, String reason, int suspicion) {
        long now = System.currentTimeMillis();
        Long last = lastAlert.get(id);
        if (last != null && now - last < ALERT_COOLDOWN_MS) return;
        lastAlert.put(id, now);

        if (notifyStaff.isEnabled()) {
            ChatUtil.warn("§c[KillauraDetector] §e" + name + " §f— " + reason);
        }
        SuspicionTracker.addSuspicion(id, name, suspicion, "KillauraDetector");
    }
}
