package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public class TreeFeller extends Module {

    private final IntSetting maxLogs = register(new IntSetting(
            "Max Logs", "Maximum logs to break per activation", 64, 8, 256));

    private int ticker = 0;

    public TreeFeller() {
        super("TreeFeller", "Breaks an entire tree when you mine the base log", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < 4) return;
        ticker = 0;

        BlockPos target = getTargetLog();
        if (target == null) return;

        Set<BlockPos> logs = floodFillLogs(target);
        int count = 0;
        for (BlockPos pos : logs) {
            if (count++ >= maxLogs.get()) break;
            mc.interactionManager.attackBlock(pos, Direction.DOWN);
        }
    }

    private BlockPos getTargetLog() {
        if (mc.crosshairTarget == null) return null;
        if (!(mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult bhr)) return null;
        BlockPos pos = bhr.getBlockPos();
        return isLog(mc.world.getBlockState(pos)) ? pos : null;
    }

    private Set<BlockPos> floodFillLogs(BlockPos start) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && visited.size() < maxLogs.get()) {
            BlockPos cur = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = cur.offset(dir);
                if (visited.contains(neighbor)) continue;
                if (!isLog(mc.world.getBlockState(neighbor))) continue;
                visited.add(neighbor);
                queue.add(neighbor);
            }
        }
        return visited;
    }

    private boolean isLog(BlockState state) {
        Block b = state.getBlock();
        return b == Blocks.OAK_LOG || b == Blocks.BIRCH_LOG || b == Blocks.SPRUCE_LOG
            || b == Blocks.JUNGLE_LOG || b == Blocks.ACACIA_LOG || b == Blocks.DARK_OAK_LOG
            || b == Blocks.MANGROVE_LOG || b == Blocks.CHERRY_LOG || b == Blocks.BAMBOO_BLOCK
            || b == Blocks.OAK_WOOD || b == Blocks.BIRCH_WOOD || b == Blocks.SPRUCE_WOOD
            || b == Blocks.JUNGLE_WOOD || b == Blocks.ACACIA_WOOD || b == Blocks.DARK_OAK_WOOD
            || b == Blocks.MANGROVE_WOOD || b == Blocks.CHERRY_WOOD
            || b == Blocks.STRIPPED_OAK_LOG || b == Blocks.STRIPPED_BIRCH_LOG
            || b == Blocks.STRIPPED_SPRUCE_LOG || b == Blocks.STRIPPED_JUNGLE_LOG
            || b == Blocks.STRIPPED_ACACIA_LOG || b == Blocks.STRIPPED_DARK_OAK_LOG
            || b == Blocks.STRIPPED_MANGROVE_LOG || b == Blocks.STRIPPED_CHERRY_LOG;
    }
}
