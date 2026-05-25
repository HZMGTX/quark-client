package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Replant extends Module {

    private int ticker = 0;

    public Replant() {
        super("Replant", "Automatically replants seeds when you break a fully-grown crop", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < 5) return;
        ticker = 0;

        BlockPos center = mc.player.getBlockPos();
        int r = 3;

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, 1, r))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.FARMLAND)) continue;
            BlockPos above = pos.up();
            if (!mc.world.getBlockState(above).isAir()) continue;

            Item seed = findSeed();
            if (seed == null) continue;

            int seedSlot = findSeedSlot(seed);
            if (seedSlot < 0) continue;

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = seedSlot;

            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(pos), Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

            mc.player.getInventory().selectedSlot = prevSlot;
            return;
        }
    }

    private Item findSeed() {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            Item item = inv.getStack(i).getItem();
            if (isSeed(item)) return item;
        }
        return null;
    }

    private int findSeedSlot(Item seed) {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(seed)) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (inv.getStack(i).isOf(seed)) return i;
        }
        return -1;
    }

    private boolean isSeed(Item item) {
        return item == Items.WHEAT_SEEDS
            || item == Items.CARROT
            || item == Items.POTATO
            || item == Items.BEETROOT_SEEDS
            || item == Items.MELON_SEEDS
            || item == Items.PUMPKIN_SEEDS
            || item == Items.NETHER_WART;
    }
}
