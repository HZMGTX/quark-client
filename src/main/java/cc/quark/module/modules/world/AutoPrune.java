package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Set;

/**
 * AutoPrune - automatically breaks leaf blocks and optionally trims
 * floating/detached tree branches for clean wood farming.
 *
 * Scans a configurable radius for leaf blocks and attacks them, leaving
 * clean log columns. Useful after tree felling to remove remaining foliage
 * without manual cleanup.
 */
public class AutoPrune extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Radius to scan for leaf blocks", 5, 1, 10));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between block breaks", 100, 50, 1000));
    private final BoolSetting includeNaturalLeaves = register(new BoolSetting(
            "Natural Leaves", "Break leaves with persistent=false (natural/decaying)", true));
    private final BoolSetting includePersistentLeaves = register(new BoolSetting(
            "Persistent Leaves", "Break player-placed (persistent=true) leaf blocks", false));
    private final BoolSetting breakFloatingLogs = register(new BoolSetting(
            "Floating Logs", "Break log blocks that have no log below them (floating)", false));
    private final DoubleSetting reachExtension = register(new DoubleSetting(
            "Reach", "Extra reach beyond the range for breaking", 0.0, 0.0, 3.0));

    private final TimerUtil timer = new TimerUtil();

    private static final Set<Block> LEAF_BLOCKS = Set.of(
            Blocks.OAK_LEAVES,   Blocks.SPRUCE_LEAVES,  Blocks.BIRCH_LEAVES,
            Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES,  Blocks.DARK_OAK_LEAVES,
            Blocks.MANGROVE_LEAVES, Blocks.CHERRY_LEAVES, Blocks.AZALEA_LEAVES,
            Blocks.FLOWERING_AZALEA_LEAVES
    );

    private static final Set<Block> LOG_BLOCKS = Set.of(
            Blocks.OAK_LOG,   Blocks.SPRUCE_LOG,   Blocks.BIRCH_LOG,
            Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG,   Blocks.DARK_OAK_LOG,
            Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG,
            Blocks.OAK_WOOD,  Blocks.SPRUCE_WOOD,  Blocks.BIRCH_WOOD,
            Blocks.JUNGLE_WOOD, Blocks.ACACIA_WOOD,  Blocks.DARK_OAK_WOOD
    );

    public AutoPrune() {
        super("AutoPrune", "Breaks leaves and floating logs for clean tree farming", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();
        double maxDist = r + reachExtension.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (mc.player.getPos().distanceTo(pos.toCenterPos()) > maxDist) continue;

            var state = mc.world.getBlockState(pos);
            Block block = state.getBlock();

            boolean shouldBreak = false;

            if (LEAF_BLOCKS.contains(block)) {
                boolean persistent = state.contains(net.minecraft.state.property.Properties.PERSISTENT)
                        && state.get(net.minecraft.state.property.Properties.PERSISTENT);
                if (!persistent && includeNaturalLeaves.isEnabled()) shouldBreak = true;
                if (persistent  && includePersistentLeaves.isEnabled()) shouldBreak = true;
            }

            if (breakFloatingLogs.isEnabled() && LOG_BLOCKS.contains(block)) {
                BlockPos below = pos.down();
                if (!LOG_BLOCKS.contains(mc.world.getBlockState(below).getBlock())
                        && mc.world.getBlockState(below).isAir()) {
                    shouldBreak = true;
                }
            }

            if (shouldBreak) {
                mc.interactionManager.attackBlock(pos.toImmutable(), Direction.DOWN);
                mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                timer.reset();
                return;
            }
        }
    }
}
