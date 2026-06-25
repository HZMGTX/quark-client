package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

/**
 * VeinMine - When mining an ore block, auto-mines all connected ore blocks of the same type.
 * Triggered when holding a pickaxe and attacking a target ore block.
 */
public class VeinMine extends Module {

    private final IntSetting maxBlocks = register(new IntSetting(
            "MaxBlocks", "Maximum connected blocks to mine", 32, 1, 128));
    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Max BFS expansion range from origin", 1.5, 1.0, 3.0));

    private final TimerUtil timer = new TimerUtil();
    private final Queue<BlockPos> queue = new LinkedList<>();
    private Block targetBlock = null;

    private static final Set<Block> ORES = Set.of(
            Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
            Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE,
            Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.NETHER_QUARTZ_ORE, Blocks.ANCIENT_DEBRIS,
            Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE
    );

    public VeinMine() {
        super("VeinMine", "Mines all connected ore blocks at once", Category.WORLD);
    }

    @Override
    public void onEnable() {
        queue.clear();
        targetBlock = null;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Only when holding a pickaxe
        if (!(mc.player.getMainHandStack().getItem() instanceof PickaxeItem)) return;

        // If queue is empty, detect what the player is looking at
        if (queue.isEmpty()) {
            var hit = mc.crosshairTarget;
            if (!(hit instanceof net.minecraft.util.hit.BlockHitResult bhr)) return;
            Block b = mc.world.getBlockState(bhr.getBlockPos()).getBlock();
            if (!ORES.contains(b)) return;

            targetBlock = b;
            expandVein(bhr.getBlockPos());
        }

        if (!timer.hasReached(50)) return;
        timer.reset();

        BlockPos next = queue.poll();
        if (next == null) return;

        if (!mc.world.getBlockState(next).isOf(targetBlock)) return;

        mc.interactionManager.attackBlock(next, Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void expandVein(BlockPos origin) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> bfs = new LinkedList<>();
        bfs.add(origin);
        visited.add(origin);

        double maxDist = range.get();

        while (!bfs.isEmpty() && queue.size() < maxBlocks.get()) {
            BlockPos cur = bfs.poll();
            queue.add(cur);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = cur.offset(dir);
                if (visited.contains(neighbor)) continue;
                if (neighbor.getSquaredDistance(origin) > maxDist * maxDist * 9) continue;
                if (mc.world.getBlockState(neighbor).isOf(targetBlock)) {
                    visited.add(neighbor);
                    bfs.add(neighbor);
                }
            }
        }
    }
}
