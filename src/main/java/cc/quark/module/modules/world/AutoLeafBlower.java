package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AutoLeafBlower extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius to scan for leaf blocks", 5, 1, 8));
    private final BoolSetting onlyDecaying = register(new BoolSetting(
            "Only Decaying", "Only break leaves that would naturally decay", false));
    private final TimerUtil timer = new TimerUtil();

    private static final Set<Block> LEAVES = new HashSet<>(Arrays.asList(
            Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES,
            Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES,
            Blocks.MANGROVE_LEAVES, Blocks.CHERRY_LEAVES, Blocks.AZALEA_LEAVES,
            Blocks.FLOWERING_AZALEA_LEAVES
    ));

    public AutoLeafBlower() {
        super("AutoLeafBlower", "Breaks leaf blocks around trees automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(80)) return;

        int r = radius.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, r + 2, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > r * r) continue;
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!LEAVES.contains(block)) continue;

            mc.interactionManager.attackBlock(pos, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            return;
        }
    }
}
