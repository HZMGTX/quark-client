package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class TeleportHistory extends Module {

    private final DoubleSetting teleportThreshold = register(new DoubleSetting(
            "TP Threshold", "Minimum block distance to count as a teleport", 10.0, 2.0, 100.0));
    private final IntSetting historySize = register(new IntSetting(
            "History Size", "Max teleport events kept per player", 20, 5, 100));
    private final BoolSetting printInChat = register(new BoolSetting(
            "Print Events", "Display teleport events in local chat as they occur", true));
    private final StringSetting filterPlayer = register(new StringSetting(
            "Filter Player", "Only log events for this player name (blank = all)", ""));

    private final Map<String, Vec3d> lastPos = new HashMap<>();
    private final Map<String, Deque<String>> history = new HashMap<>();

    public TeleportHistory() {
        super("TeleportHistory", "Logs all teleport events for players", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        lastPos.clear();
        history.clear();
        ChatUtil.info("§6[TeleportHistory] §fLogging teleport events (threshold: §e"
                + teleportThreshold.get() + " blocks§f).");
    }

    @Override
    public void onDisable() {
        printHistory();
        lastPos.clear();
        history.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        String filter = filterPlayer.get().trim().toLowerCase();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            String name = player.getName().getString();

            if (!filter.isEmpty() && !name.equalsIgnoreCase(filter)) continue;

            Vec3d pos = player.getPos();

            if (lastPos.containsKey(name)) {
                Vec3d prev = lastPos.get(name);
                double dist = prev.distanceTo(pos);

                if (dist >= teleportThreshold.get()) {
                    String entry = String.format("[%s] %.1f,%.1f,%.1f -> %.1f,%.1f,%.1f (%.1f blocks)",
                            name,
                            prev.x, prev.y, prev.z,
                            pos.x,  pos.y,  pos.z,
                            dist);

                    Deque<String> log = history.computeIfAbsent(name, k -> new ArrayDeque<>());
                    if (log.size() >= historySize.get()) log.pollFirst();
                    log.addLast(entry);

                    if (printInChat.isEnabled()) {
                        ChatUtil.info("§6[TeleportHistory] §7" + entry);
                    }
                }
            }

            lastPos.put(name, pos);
        }
    }

    private void printHistory() {
        if (history.isEmpty()) {
            ChatUtil.info("§6[TeleportHistory] §7No teleport events recorded.");
            return;
        }
        ChatUtil.info("§6[TeleportHistory] §f--- Teleport Log ---");
        for (Map.Entry<String, Deque<String>> entry : history.entrySet()) {
            ChatUtil.info("§e" + entry.getKey() + "§7: §f" + entry.getValue().size() + " event(s)");
            for (String ev : entry.getValue()) {
                ChatUtil.info("  §7" + ev);
            }
        }
    }
}
