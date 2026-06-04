package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AutoChop extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect and chop logs", 4.0, 1.0, 6.0));
    private final BoolSetting replant = register(new BoolSetting(
            "Replant", "Replant saplings after chopping", false));

    private static final Set<Block> LOGS = new HashSet<>(Arrays.asList(
            Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG,
            Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG,
            Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG
    ));

    private final TimerUtil timer = new TimerUtil();

    public AutoChop() {
        super("AutoChop", "Auto-chops trees when axe in hand", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;

        // Only act when holding an axe
        ItemStack held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof AxeItem)) return;

        timer.reset();

        BlockPos center = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, r + 3, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > range.get() * range.get()) continue;
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!LOGS.contains(block)) continue;

            mc.interactionManager.attackBlock(pos, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);

            if (replant.isEnabled()) {
                tryReplant(pos.down(), block);
            }
            return;
        }
    }

    private void tryReplant(BlockPos soil, Block chopBlock) {
        if (mc.world == null || mc.interactionManager == null) return;
        if (!mc.world.getBlockState(soil).isOf(Blocks.GRASS_BLOCK) &&
            !mc.world.getBlockState(soil).isOf(Blocks.DIRT)) return;

        Item sapling = getSapling(chopBlock);
        if (sapling == null) return;

        int slot = findInHotbar(sapling);
        if (slot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(soil), Direction.UP, soil, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.getInventory().selectedSlot = prev;
    }

    private Item getSapling(Block log) {
        if (log == Blocks.OAK_LOG)      return Items.OAK_SAPLING;
        if (log == Blocks.SPRUCE_LOG)   return Items.SPRUCE_SAPLING;
        if (log == Blocks.BIRCH_LOG)    return Items.BIRCH_SAPLING;
        if (log == Blocks.JUNGLE_LOG)   return Items.JUNGLE_SAPLING;
        if (log == Blocks.ACACIA_LOG)   return Items.ACACIA_SAPLING;
        if (log == Blocks.DARK_OAK_LOG) return Items.DARK_OAK_SAPLING;
        return null;
    }

    private int findInHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}
