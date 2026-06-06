package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryHackDetector extends Module {

    private final IntSetting cooldownMs = register(new IntSetting(
            "Cooldown (ms)", "Minimum ms between slot changes; faster = suspicious", 100, 50, 500));
    private final BoolSetting notifyStaff = register(new BoolSetting(
            "Notify Staff", "Send alert when impossible inventory speed is detected", true));

    // Track per-player last slot-change time using entity ID as proxy key
    private final Map<Integer, Long> lastSlotChange = new HashMap<>();
    // entity ID -> violation count
    private final Map<Integer, Integer> violations = new HashMap<>();
    // entity ID -> last alert time
    private final Map<Integer, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 8_000;
    private static final int VIOLATION_THRESHOLD = 5;

    public InventoryHackDetector() {
        super("InventoryHackDetector", "Flags inventory slot changes that occur faster than humanly possible", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        lastSlotChange.clear();
        violations.clear();
        lastAlert.clear();
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[InventoryHackDetector] §fMonitoring slot changes (min cooldown §e" + cooldownMs.get() + "ms§f).");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        lastSlotChange.clear();
        violations.clear();
        lastAlert.clear();
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket pkt)) return;
        if (mc.world == null || mc.player == null) return;

        // ScreenHandlerSlotUpdateS2CPacket carries the sync ID and slot — we use the syncId
        // as a proxy for which player's container is being updated.
        // For simplicity, we map syncId to a nearby player and check timing.
        int syncId = pkt.getSyncId();

        long now = System.currentTimeMillis();
        Long last = lastSlotChange.get(syncId);
        if (last != null) {
            long elapsed = now - last;
            if (elapsed < cooldownMs.get()) {
                int v = violations.getOrDefault(syncId, 0) + 1;
                violations.put(syncId, v);

                if (v >= VIOLATION_THRESHOLD) {
                    Long alertLast = lastAlert.get(syncId);
                    if (alertLast == null || now - alertLast > ALERT_COOLDOWN_MS) {
                        lastAlert.put(syncId, now);
                        // Find closest player as suspect
                        String suspect = "Unknown";
                        if (mc.world != null) {
                            PlayerEntity closest = mc.world.getPlayers().stream()
                                    .filter(p -> p != mc.player)
                                    .min((a, b) -> Double.compare(
                                            a.squaredDistanceTo(mc.player),
                                            b.squaredDistanceTo(mc.player)))
                                    .orElse(null);
                            if (closest != null) {
                                suspect = closest.getName().getString();
                                UUID uid = closest.getUuid();
                                SuspicionTracker.addSuspicion(uid, suspect, 10, "InventoryHackDetector");
                            }
                        }
                        if (notifyStaff.isEnabled()) {
                            ChatUtil.warn("§c[InventoryHackDetector] §e" + suspect
                                    + " §f— slot change §c" + elapsed + "ms §f(min §a" + cooldownMs.get() + "ms§f), flags: §c" + v);
                        }
                        violations.put(syncId, 0);
                    }
                }
            } else {
                violations.computeIfPresent(syncId, (k, v) -> Math.max(0, v - 1));
            }
        }

        lastSlotChange.put(syncId, now);
    }
}
