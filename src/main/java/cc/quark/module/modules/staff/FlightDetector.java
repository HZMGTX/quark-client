package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlightDetector extends Module {

    private final DoubleSetting yThreshold = register(new DoubleSetting(
            "Y Threshold", "Upward Y velocity considered suspicious (blocks/tick)", 1.5, 0.5, 3.0));
    private final DoubleSetting speedThreshold = register(new DoubleSetting(
            "Speed Threshold", "Horizontal speed considered suspicious (blocks/tick)", 1.2, 0.5, 5.0));
    private final BoolSetting autoKick = register(new BoolSetting(
            "Auto Kick", "Automatically kick confirmed flight hackers", false));

    // UUID -> consecutive suspicious ticks
    private final Map<UUID, Integer> airViolations = new HashMap<>();
    // UUID -> last position
    private final Map<UUID, Vec3d> lastPos = new HashMap<>();
    // UUID -> last alert time
    private final Map<UUID, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 12_000;
    private static final int VIOLATION_THRESHOLD = 10;

    public FlightDetector() {
        super("FlightDetector", "Detects players flying without permission via velocity anomalies", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        airViolations.clear();
        lastPos.clear();
        lastAlert.clear();
        ChatUtil.info("§6[FlightDetector] §fMonitoring vertical/horizontal velocity anomalies.");
    }

    @Override
    public void onDisable() {
        airViolations.clear();
        lastPos.clear();
        lastAlert.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            UUID id = player.getUuid();
            String name = player.getName().getString();
            Vec3d pos = player.getPos();

            // Skip players with creative flight, levitation, or slow falling
            if (player.getAbilities().flying) { reset(id, pos); continue; }
            if (player.hasStatusEffect(StatusEffects.LEVITATION)) { reset(id, pos); continue; }
            if (player.hasStatusEffect(StatusEffects.SLOW_FALLING)) { reset(id, pos); continue; }
            if (player.isTouchingWater() || player.isInLava()) { reset(id, pos); continue; }

            Vec3d prev = lastPos.get(id);
            if (prev != null) {
                double dx = pos.x - prev.x;
                double dy = pos.y - prev.y;
                double dz = pos.z - prev.z;
                double horizSpeed = Math.sqrt(dx * dx + dz * dz);

                boolean suspiciousY = dy > yThreshold.get();
                boolean suspiciousSpeed = horizSpeed > speedThreshold.get() && !player.isOnGround();
                boolean hovering = !player.isOnGround() && Math.abs(dy) < 0.02 && horizSpeed < 0.05;

                if (suspiciousY || suspiciousSpeed || hovering) {
                    int violations = airViolations.getOrDefault(id, 0) + 1;
                    airViolations.put(id, violations);

                    if (violations >= VIOLATION_THRESHOLD) {
                        long now = System.currentTimeMillis();
                        Long last = lastAlert.get(id);
                        if (last == null || now - last > ALERT_COOLDOWN_MS) {
                            lastAlert.put(id, now);
                            String detail = hovering ? "hovering" : suspiciousY
                                    ? String.format("upward Y=%.2f", dy)
                                    : String.format("air-speed=%.2f b/t", horizSpeed);
                            ChatUtil.warn("§c[FlightDetector] §e" + name + " §fis likely flying — " + detail);
                            SuspicionTracker.addSuspicion(id, name, 25, "FlightDetector");

                            if (autoKick.isEnabled()) {
                                mc.player.networkHandler.sendChatCommand("kick " + name + " Illegal flight detected");
                                ChatUtil.info("§6[FlightDetector] §fKicked §e" + name + "§f.");
                            }
                            airViolations.put(id, 0);
                        }
                    }
                } else {
                    // Decay violations when behavior normalizes
                    airViolations.computeIfPresent(id, (k, v) -> Math.max(0, v - 1));
                }
            }

            lastPos.put(id, pos);
        }
    }

    private void reset(UUID id, Vec3d pos) {
        lastPos.put(id, pos);
        airViolations.put(id, 0);
    }
}
