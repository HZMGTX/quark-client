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
import net.minecraft.item.AxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

public class TreeChop extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to find tree bases", 4.0, 1.0, 8.0));
    private final IntSetting maxBlocks = register(new IntSetting(
            "MaxBlocks", "Max log blocks to break per activation", 64, 8, 128));

    private static final Set<Block> LOGS = new HashSet<>(Arrays.asList(
            Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG,
            Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG,
            Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG,
            Blocks.OAK_WOOD, Blocks.SPRUCE_WOOD, Blocks.BIRCH_WOOD,
            Blocks.JUNGLE_WOOD, Blocks.ACACIA_WOOD, Blocks.DARK_OAK_WOOD
    ));

    private final TimerUtil timer = new TimerUtil();
    private final List<BlockPos> breakQueue = new ArrayList<>();
    private boolean scanning = false;

    public TreeChop() {
        super("TreeChop", "Chops entire trees by breaking base and queuing all connected logs", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
        breakQueue.clear();
        scanning = false;
    }

    @Override
    public void onDisable() {
        breakQueue.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Process break queue first
        if (!breakQueue.isEmpty()) {
            if (!timer.hasReached(100)) return;
            timer.reset();
            BlockPos next = breakQueue.remove(0);
            if (mc.world.getBlockState(next).getBlock() instanceof Block b && LOGS.contains(b)) {
                mc.interactionManager.attackBlock(next, Direction.DOWN);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            return;
        }

        if (!timer.hasReached(500)) return;
        timer.reset();

        if (!(mc.player.getMainHandStack().getItem() instanceof AxeItem)) return;

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        // Find a log base (log with non-log below it)
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, 1, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            Block b = mc.world.getBlockState(pos).getBlock();
            if (!LOGS.contains(b)) continue;
            Block below = mc.world.getBlockState(pos.down()).getBlock();
            if (LOGS.contains(below)) continue;

            // Found a tree base — flood-fill all connected logs
            floodFillTree(pos);
            return;
        }
    }

    private void floodFillTree(BlockPos base) {
        if (mc.world == null) return;
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> stack = new ArrayDeque<>();
        stack.push(base);
        int limit = maxBlocks.get();

        while (!stack.isEmpty() && visited.size() < limit) {
            BlockPos pos = stack.pop();
            if (visited.contains(pos)) continue;
            visited.add(pos);
            breakQueue.add(pos.toImmutable());

            // Check all 6 neighbors + diagonals above for connected logs
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                if (!visited.contains(neighbor) && LOGS.contains(mc.world.getBlockState(neighbor).getBlock())) {
                    stack.push(neighbor);
                }
            }
        }
    }
}
