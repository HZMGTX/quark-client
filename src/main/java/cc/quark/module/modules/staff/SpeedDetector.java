package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpeedDetector extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "How many times normal walk speed triggers a violation", 2.0, 1.2, 3.0));
    private final IntSetting alertThreshold = register(new IntSetting(
            "Alert Threshold", "Consecutive violations before alerting staff", 5, 3, 10));
    private final BoolSetting autoKick = register(new BoolSetting(
            "Auto Kick", "Kick player automatically after threshold is exceeded", false));

    // Vanilla walk speed in blocks/tick
    private static final double BASE_WALK_SPEED = 0.215;
    // Vanilla sprint speed in blocks/tick
    private static final double BASE_SPRINT_SPEED = 0.28;

    private final Map<UUID, Integer> violations = new HashMap<>();
    private final Map<UUID, Vec3d> lastPos = new HashMap<>();
    private final Map<UUID, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 10_000;

    public SpeedDetector() {
        super("SpeedDetector", "Compares expected vs actual movement per tick to detect speed hacks", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        violations.clear();
        lastPos.clear();
        lastAlert.clear();
        ChatUtil.info("§6[SpeedDetector] §fChecking player speeds (§e" + multiplier.get() + "x§f threshold).");
    }

    @Override
    public void onDisable() {
        violations.clear();
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
            Vec3d prev = lastPos.get(id);

            if (prev != null && player.isOnGround()) {
                double dx = pos.x - prev.x;
                double dz = pos.z - prev.z;
                double actual = Math.sqrt(dx * dx + dz * dz);

                // Scale expected speed by Speed effect amplifier if present
                double expected = player.isSprinting() ? BASE_SPRINT_SPEED : BASE_WALK_SPEED;
                if (player.hasStatusEffect(StatusEffects.SPEED)) {
                    int amp = player.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1;
                    expected *= 1.0 + (amp * 0.2);
                }

                double limit = expected * multiplier.get();
                if (actual > limit) {
                    int v = violations.getOrDefault(id, 0) + 1;
                    violations.put(id, v);

                    if (v >= alertThreshold.get()) {
                        long now = System.currentTimeMillis();
                        Long last = lastAlert.get(id);
                        if (last == null || now - last > ALERT_COOLDOWN_MS) {
                            lastAlert.put(id, now);
                            ChatUtil.warn(String.format(
                                    "§c[SpeedDetector] §e%s §f— actual §c%.3f §fb/t vs expected §a%.3f§f (x%.1f)",
                                    name, actual, expected, actual / expected));
                            SuspicionTracker.addSuspicion(id, name, 20, "SpeedDetector");

                            if (autoKick.isEnabled()) {
                                mc.player.networkHandler.sendChatCommand("kick " + name + " Speed hacking");
                                ChatUtil.info("§6[SpeedDetector] §fKicked §e" + name + "§f.");
                            }
                            violations.put(id, 0);
                        }
                    }
                } else {
                    violations.computeIfPresent(id, (k, v) -> Math.max(0, v - 1));
                }
            }

            lastPos.put(id, pos);
        }
    }
}
