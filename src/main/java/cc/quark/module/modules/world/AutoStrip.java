package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Set;

public class AutoStrip extends Module {

    private final BoolSetting autoReplant = register(new BoolSetting(
            "AutoReplant", "Replant saplings after stripping", false));

    private final TimerUtil timer = new TimerUtil();

    private static final Set<Block> LOGS = Set.of(
            Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG,
            Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG,
            Blocks.CHERRY_LOG, Blocks.BAMBOO_BLOCK, Blocks.CRIMSON_STEM, Blocks.WARPED_STEM
    );

    public AutoStrip() {
        super("AutoStrip", "Strip-mines logs automatically when encountered", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;
        timer.reset();

        int axeSlot = findAxeSlot();
        if (axeSlot == -1) return;

        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-4, -2, -4), center.add(4, 4, 4))) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!LOGS.contains(block)) continue;

            int saved = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = axeSlot;
            mc.interactionManager.attackBlock(pos, Direction.UP);
            mc.player.getInventory().selectedSlot = saved;
            return;
        }
    }

    private int findAxeSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem().toString().contains("_axe")) return i;
        }
        return -1;
    }
}
