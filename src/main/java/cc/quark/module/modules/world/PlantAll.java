package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PlantAll extends Module {

    private final BoolSetting autoTill = register(new BoolSetting("AutoTill", "Automatically till dirt/grass before planting", false));

    private final TimerUtil timer = new TimerUtil();

    public PlantAll() {
        super("PlantAll", "Plants seeds on tilled farmland automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(80)) return;

        BlockPos center = mc.player.getBlockPos();

        if (autoTill.isEnabled()) {
            int hoeSlot = findItemSlot(HoeItem.class);
            if (hoeSlot != -1) {
                for (BlockPos pos : BlockPos.iterate(center.add(-5, -2, -5), center.add(5, 0, 5))) {
                    var block = mc.world.getBlockState(pos).getBlock();
                    if (block != Blocks.GRASS_BLOCK && block != Blocks.DIRT) continue;
                    if (!mc.world.getBlockState(pos.up()).isAir()) continue;
                    int saved = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = hoeSlot;
                    BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.player.getInventory().selectedSlot = saved;
                    timer.reset();
                    return;
                }
            }
        }

        for (BlockPos pos : BlockPos.iterate(center.add(-5, -2, -5), center.add(5, 0, 5))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.FARMLAND)) continue;
            if (!mc.world.getBlockState(pos.up()).isAir()) continue;
            int seedSlot = findSeedSlot();
            if (seedSlot == -1) return;
            int saved = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = seedSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = saved;
            timer.reset();
            return;
        }
    }

    private int findSeedSlot() {
        if (mc.player == null) return -1;
        Item[] seeds = {Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS,
                Items.MELON_SEEDS, Items.PUMPKIN_SEEDS};
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            for (Item seed : seeds) if (item == seed) return i;
        }
        for (int i = 9; i < 36; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            for (Item seed : seeds) if (item == seed) return i;
        }
        return -1;
    }

    private int findItemSlot(Class<?> itemClass) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (itemClass.isInstance(mc.player.getInventory().getStack(i).getItem())) return i;
        }
        return -1;
    }
}
