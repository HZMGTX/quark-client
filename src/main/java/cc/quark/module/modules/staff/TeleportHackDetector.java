package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportHackDetector extends Module {

    private final DoubleSetting maxDelta = register(new DoubleSetting(
            "Max Delta", "Maximum position change per tick before flagging (blocks)", 15.0, 5.0, 50.0));
    private final BoolSetting logPositions = register(new BoolSetting(
            "Log Positions", "Print from/to coordinates on each flagged teleport", true));

    // UUID -> last known position
    private final Map<UUID, Vec3d> lastPos = new HashMap<>();
    // UUID -> flag count this session
    private final Map<UUID, Integer> flags = new HashMap<>();
    // UUID -> last alert time
    private final Map<UUID, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 6_000;

    public TeleportHackDetector() {
        super("TeleportHackDetector", "Detects position delta spikes indicating teleport or blink hacks", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        lastPos.clear();
        flags.clear();
        lastAlert.clear();
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[TeleportHackDetector] §fMonitoring position deltas (max §e" + maxDelta.get() + " blocks/tick§f).");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        lastPos.clear();
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
            Vec3d pos = player.getPos();
            Vec3d prev = lastPos.get(id);

            if (prev != null) {
                double delta = pos.distanceTo(prev);

                if (delta > maxDelta.get()) {
                    int f = flags.getOrDefault(id, 0) + 1;
                    flags.put(id, f);

                    long now = System.currentTimeMillis();
                    Long last = lastAlert.get(id);
                    if (last == null || now - last > ALERT_COOLDOWN_MS) {
                        lastAlert.put(id, now);

                        if (logPositions.isEnabled()) {
                            ChatUtil.warn(String.format(
                                    "§c[TeleportHackDetector] §e%s §fteleported §c%.1f§f blocks in 1 tick! §7(%.0f,%.0f,%.0f → %.0f,%.0f,%.0f) flags:%d",
                                    name, delta,
                                    prev.x, prev.y, prev.z,
                                    pos.x, pos.y, pos.z, f));
                        } else {
                            ChatUtil.warn("§c[TeleportHackDetector] §e" + name
                                    + " §fposition spike §c" + String.format("%.1f", delta) + "§f blocks — flags: " + f);
                        }
                        SuspicionTracker.addSuspicion(id, name, 22, "TeleportHackDetector");
                        AntiCheatMonitor.logEvent(name, "TeleportHack", String.format("%.1f blocks", delta));
                    }
                }
            }

            lastPos.put(id, pos);
        }
    }
}
