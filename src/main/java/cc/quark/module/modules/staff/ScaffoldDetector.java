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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScaffoldDetector extends Module {

    private final IntSetting placeCooldown = register(new IntSetting(
            "Place Cooldown", "Min ticks between blocks before flagging as scaffold", 2, 1, 5));
    private final BoolSetting notifyStaff = register(new BoolSetting(
            "Notify Staff", "Send staff alert when scaffold pattern is detected", true));

    // UUID -> list of [lastPlaceTick, consecutivePlaceCount]
    private final Map<UUID, int[]> placeData = new HashMap<>();
    // UUID -> last block placed Y level (to detect tower building)
    private final Map<UUID, Double> lastPlaceY = new HashMap<>();
    // UUID -> last alert time
    private final Map<UUID, Long> lastAlert = new HashMap<>();

    private static final long ALERT_COOLDOWN_MS = 8_000;
    private static final int CONSECUTIVE_THRESHOLD = 6;

    private int tick = 0;

    public ScaffoldDetector() {
        super("ScaffoldDetector", "Detects scaffold/tower block placement patterns at player feet level", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        placeData.clear();
        lastPlaceY.clear();
        lastAlert.clear();
        tick = 0;
        ChatUtil.info("§6[ScaffoldDetector] §fWatching fast block placement (cooldown ≤§e" + placeCooldown.get() + "§f ticks).");
    }

    @Override
    public void onDisable() {
        placeData.clear();
        lastPlaceY.clear();
        lastAlert.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.world == null || mc.player == null) return;
        tick++;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            UUID id = player.getUuid();
            String name = player.getName().getString();

            // Detect block placed directly under the player (scaffold / tower pattern)
            BlockPos feetPos = player.getBlockPos().down();
            var blockBelow = mc.world.getBlockState(feetPos).getBlock();
            boolean placedBelow = (blockBelow != Blocks.AIR && blockBelow != Blocks.CAVE_AIR);

            if (placedBelow) {
                Double prevY = lastPlaceY.get(id);
                double curY = player.getY();

                // Tower: player placed a block and moved up at least 0.9 blocks
                boolean isTowering = prevY != null && curY - prevY > 0.9;

                if (isTowering) {
                    int[] data = placeData.computeIfAbsent(id, k -> new int[]{tick, 0});
                    int ticksSinceLast = tick - data[0];
                    data[0] = tick;

                    if (ticksSinceLast <= placeCooldown.get()) {
                        data[1]++;
                    } else {
                        data[1] = 0;
                    }

                    if (data[1] >= CONSECUTIVE_THRESHOLD) {
                        long now = System.currentTimeMillis();
                        Long last = lastAlert.get(id);
                        if (last == null || now - last > ALERT_COOLDOWN_MS) {
                            lastAlert.put(id, now);
                            if (notifyStaff.isEnabled()) {
                                ChatUtil.warn("§c[ScaffoldDetector] §e" + name
                                        + " §fis §ctowering/scaffolding §f(§e" + data[1] + " §fblocks at ≤§e"
                                        + placeCooldown.get() + "§f tick cooldown)");
                            }
                            SuspicionTracker.addSuspicion(id, name, 12, "ScaffoldDetector");
                            data[1] = 0;
                        }
                    }
                }
                lastPlaceY.put(id, curY);
            }
        }
    }
}
