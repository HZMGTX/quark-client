package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.*;

public class TreeChopper extends Module {
    private final IntSetting maxLogs = register(new IntSetting("Max Logs", "Max log blocks to chop", 32, 1, 128));
    private final IntSetting delay = register(new IntSetting("Delay", "Delay between breaks (ms)", 100, 10, 500));
    private final TimerUtil timer = new TimerUtil();
    private final List<BlockPos> logQueue = new ArrayList<>();

    public TreeChopper() { super("TreeChopper", "Chops entire trees by breaking one log", Category.WORLD); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); logQueue.clear(); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); logQueue.clear(); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        if (logQueue.isEmpty()) {
            if (mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult bhr) {
                BlockPos pos = bhr.getBlockPos();
                if (isLog(mc.world.getBlockState(pos).getBlock())) findTree(pos);
            }
            return;
        }

        BlockPos next = logQueue.remove(0);
        mc.interactionManager.attackBlock(next, Direction.DOWN);
        timer.reset();
    }

    private boolean isLog(Block b) {
        return b instanceof PillarBlock && (b.toString().contains("log") || b.toString().contains("stem"));
    }

    private void findTree(BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(start);
        while (!queue.isEmpty() && logQueue.size() < maxLogs.get()) {
            BlockPos pos = queue.poll();
            if (!visited.add(pos)) continue;
            if (!isLog(mc.world.getBlockState(pos).getBlock())) continue;
            logQueue.add(pos);
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = 0; dy <= 1; dy++)
                    for (int dz = -1; dz <= 1; dz++)
                        queue.add(pos.add(dx, dy, dz));
        }
    }
}
