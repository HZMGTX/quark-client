package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XrayDetector extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Radius in blocks to monitor nearby players", 32, 16, 64));
    private final BoolSetting alertStaff = register(new BoolSetting(
            "Alert Staff", "Send alert message in chat when flagged", true));
    private final BoolSetting autoFlag = register(new BoolSetting(
            "Auto Flag", "Automatically accumulate suspicion points on flag", true));

    // UUID -> consecutive ore-mine-without-expose count
    private final Map<UUID, Integer> suspectCount = new HashMap<>();
    // UUID -> last known position before mining
    private final Map<UUID, Vec3d> lastPos = new HashMap<>();
    // UUID -> last alert time (ms) to throttle spam
    private final Map<UUID, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 10_000;

    public XrayDetector() {
        super("XrayDetector", "Flags players who mine directly toward ores without exposing them first", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        suspectCount.clear();
        lastPos.clear();
        lastAlert.clear();
        mc.getEventBus().subscribe(this);
        ChatUtil.info("§6[XrayDetector] §fMonitoring ore mining in §e" + range.get() + "§f block radius.");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        suspectCount.clear();
        lastPos.clear();
        lastAlert.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            double dist = player.distanceTo(mc.player);
            if (dist > range.get()) continue;

            UUID id = player.getUuid();
            Vec3d pos = player.getPos();
            Vec3d prev = lastPos.get(id);

            if (prev != null) {
                // Check if player moved significantly (likely mining)
                double moved = pos.distanceTo(prev);
                if (moved < 0.5) {
                    // Stationary — check blocks around feet for direct ore adjacency
                    BlockPos bp = player.getBlockPos();
                    boolean directOreAdjacent = isOreDirectlyAdjacent(bp);
                    boolean surroundingExposed = isSurroundingExposed(bp);

                    if (directOreAdjacent && !surroundingExposed) {
                        int count = suspectCount.getOrDefault(id, 0) + 1;
                        suspectCount.put(id, count);

                        if (count >= 3) {
                            long now = System.currentTimeMillis();
                            Long last = lastAlert.get(id);
                            if (last == null || now - last > ALERT_COOLDOWN_MS) {
                                lastAlert.put(id, now);
                                String name = player.getName().getString();
                                if (alertStaff.isEnabled()) {
                                    ChatUtil.warn("§c[XrayDetect] §e" + name + " §fmay be using Xray §7(flags: " + count + ")");
                                }
                                if (autoFlag.isEnabled()) {
                                    SuspicionTracker.addSuspicion(id, name, 15, "XrayDetector");
                                }
                            }
                        }
                    }
                }
            }

            lastPos.put(id, pos);
        }
    }

    private boolean isOreDirectlyAdjacent(BlockPos bp) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    var block = mc.world.getBlockState(bp.add(dx, dy, dz)).getBlock();
                    if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
                            || block == Blocks.ANCIENT_DEBRIS || block == Blocks.GOLD_ORE
                            || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.EMERALD_ORE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSurroundingExposed(BlockPos bp) {
        int airCount = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (mc.world.getBlockState(bp.add(dx, dy, dz)).isAir()) airCount++;
                }
            }
        }
        // If fewer than 8 air blocks nearby, it's not a naturally exposed tunnel
        return airCount >= 8;
    }
}
