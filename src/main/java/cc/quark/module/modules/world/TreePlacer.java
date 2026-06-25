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
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TreePlacer extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to look for empty spots after tree cuts", 4.0, 1.0, 8.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between sapling placements", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public TreePlacer() {
        super("TreePlacer", "Auto-places saplings where trees were cut (dirt/grass with air above)", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;

            Block below = mc.world.getBlockState(pos).getBlock();
            boolean isSoil = below == Blocks.GRASS_BLOCK || below == Blocks.DIRT
                    || below == Blocks.PODZOL || below == Blocks.COARSE_DIRT;
            if (!isSoil) continue;

            BlockPos above = pos.up();
            if (!mc.world.getBlockState(above).isAir()) continue;

            // Find a matching sapling for any nearby stump pattern
            Item sapling = findBestSapling();
            if (sapling == null) continue;

            int saplingSlot = findItemSlot(sapling);
            if (saplingSlot == -1) continue;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = saplingSlot;
            Vec3d hitVec = Vec3d.ofCenter(pos).add(0, 0.5, 0);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            return;
        }
        timer.reset();
    }

    /** Finds any sapling in the player's inventory. */
    private Item findBestSapling() {
        if (mc.player == null) return null;
        Item[] saplings = {
                Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING,
                Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING,
                Items.MANGROVE_PROPAGULE, Items.CHERRY_SAPLING
        };
        for (Item s : saplings) {
            if (findItemSlot(s) != -1) return s;
        }
        return null;
    }

    private int findItemSlot(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}
