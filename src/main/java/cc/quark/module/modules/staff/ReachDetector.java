package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReachDetector extends Module {

    private final DoubleSetting maxReach = register(new DoubleSetting(
            "Max Reach", "Maximum allowed attack reach in blocks before flagging", 4.5, 3.5, 6.0));
    private final BoolSetting alertOnViolation = register(new BoolSetting(
            "Alert On Violation", "Send chat alert every time a reach violation is detected", true));
    private final BoolSetting logViolations = register(new BoolSetting(
            "Log Violations", "Print a detailed distance log for each flagged hit", true));

    // UUID attacker -> violation count
    private final Map<UUID, Integer> violationCount = new HashMap<>();
    // UUID -> last alert time
    private final Map<UUID, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 5_000;

    public ReachDetector() {
        super("ReachDetector", "Detects players hitting others at impossible distances", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        violationCount.clear();
        lastAlert.clear();
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[ReachDetector] §fFlagging hits beyond §e" + maxReach.get() + "§f blocks.");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        violationCount.clear();
        lastAlert.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        // Cross-check all player pairs for suspiciously close arm-swing moments
        // while they are far apart (proxy for reach detection without packet injection)
        for (PlayerEntity attacker : mc.world.getPlayers()) {
            if (attacker == mc.player) continue;
            if (!attacker.handSwinging || attacker.handSwingTicks != 1) continue;

            UUID aid = attacker.getUuid();

            for (PlayerEntity victim : mc.world.getPlayers()) {
                if (victim == attacker) continue;
                double dist = attacker.distanceTo(victim);

                // Only flag if victim recently received damage (hurtTime > 0)
                if (victim.hurtTime > 0 && dist > maxReach.get()) {
                    int v = violationCount.getOrDefault(aid, 0) + 1;
                    violationCount.put(aid, v);

                    long now = System.currentTimeMillis();
                    Long last = lastAlert.get(aid);
                    if (alertOnViolation.isEnabled() && (last == null || now - last > ALERT_COOLDOWN_MS)) {
                        lastAlert.put(aid, now);
                        String aName = attacker.getName().getString();
                        String vName = victim.getName().getString();

                        if (logViolations.isEnabled()) {
                            ChatUtil.warn(String.format(
                                    "§c[ReachDetector] §e%s §fhit §e%s §fat §c%.2f§f blocks (max §a%.1f§f) — flags: §c%d",
                                    aName, vName, dist, maxReach.get(), v));
                        } else {
                            ChatUtil.warn("§c[ReachDetector] §e" + aName + " §fmay have illegal reach.");
                        }
                        SuspicionTracker.addSuspicion(aid, aName, 18, "ReachDetector");
                    }
                }
            }
        }
    }
}
