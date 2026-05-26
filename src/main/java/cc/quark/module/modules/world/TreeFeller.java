package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class TreeFeller extends Module {

    private final IntSetting maxLogs = register(new IntSetting(
            "Max Logs", "Maximum logs to break per tree", 64, 8, 256));

    private final BoolSetting includeLeaves = register(new BoolSetting(
            "Include Leaves", "Also remove leaves when chopping", false));

    private final IntSetting breakDelay = register(new IntSetting(
            "Delay", "Ticks between breaking each log", 1, 0, 3));

    private final Queue<BlockPos> logQueue = new LinkedList<>();
    private boolean active = false;
    private int delayCounter = 0;
    private BlockPos lastCrosshairLog = null;

    public TreeFeller() {
        super("TreeFeller", "Breaks an entire tree when you mine the base log", Category.WORLD);
    }

    @Override
    public void onDisable() {
        logQueue.clear();
        active = false;
        lastCrosshairLog = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (!active) {
            BlockPos target = getTargetLog();
            if (target == null) {
                lastCrosshairLog = null;
                return;
            }
            if (mc.options.attackKey.isPressed() && !target.equals(lastCrosshairLog)) {
                lastCrosshairLog = target;
                findTree(target);
                active = !logQueue.isEmpty();
            }
            return;
        }

        if (logQueue.isEmpty()) {
            active = false;
            lastCrosshairLog = null;
            return;
        }

        if (delayCounter < breakDelay.get()) {
            delayCounter++;
            return;
        }
        delayCounter = 0;

        BlockPos pos = logQueue.poll();
        if (pos == null) return;

        BlockState state = mc.world.getBlockState(pos);
        if (isLog(state) || (includeLeaves.isEnabled() && isLeaf(state))) {
            switchToAxe(state);
            mc.interactionManager.attackBlock(pos, Direction.UP);
        }

        if (logQueue.isEmpty()) {
            active = false;
            lastCrosshairLog = null;
        }
    }

    private void findTree(BlockPos start) {
        logQueue.clear();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> bfsQueue = new ArrayDeque<>();
        bfsQueue.add(start);
        visited.add(start);

        while (!bfsQueue.isEmpty() && visited.size() < maxLogs.get()) {
            BlockPos cur = bfsQueue.poll();
            BlockState state = mc.world.getBlockState(cur);
            if (!isLog(state) && !(includeLeaves.isEnabled() && isLeaf(state))) continue;
            logQueue.add(cur);
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = cur.offset(dir);
                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);
                BlockState nState = mc.world.getBlockState(neighbor);
                if (isLog(nState) || (includeLeaves.isEnabled() && isLeaf(nState))) {
                    bfsQueue.add(neighbor);
                }
            }
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = 0; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos neighbor = cur.add(dx, dy, dz);
                        if (visited.contains(neighbor)) continue;
                        visited.add(neighbor);
                        BlockState nState = mc.world.getBlockState(neighbor);
                        if (isLog(nState)) {
                            bfsQueue.add(neighbor);
                        }
                    }
                }
            }
        }
    }

    private BlockPos getTargetLog() {
        if (mc.crosshairTarget == null) return null;
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) return null;
        BlockPos pos = bhr.getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        return isLog(state) ? pos : null;
    }

    private boolean isLog(BlockState state) {
        if (state.isIn(BlockTags.LOGS)) return true;
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

    private boolean isLeaf(BlockState state) {
        return state.getBlock() instanceof LeavesBlock || state.isIn(BlockTags.LEAVES);
    }

    private void switchToAxe(BlockState state) {
        if (mc.player == null) return;
        int bestSlot = -1;
        float bestSpeed = -1f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        if (bestSlot != -1) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }
}
