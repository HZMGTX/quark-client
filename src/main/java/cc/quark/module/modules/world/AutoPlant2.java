package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AutoPlant2 extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius to scan for empty farmland", 5, 1, 10));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between planting actions", 100, 50, 1000));

    private final BoolSetting tillDirt = register(new BoolSetting(
            "TillDirt", "Till nearby dirt/grass blocks to farmland with a hoe", true));

    private final BoolSetting bonemeal = register(new BoolSetting(
            "Bonemeal", "Apply bone meal to newly planted crops", false));

    private final TimerUtil timer = new TimerUtil();

    public AutoPlant2() {
        super("AutoPlant2", "Automatically plants seeds and crops on empty farmland", Category.WORLD);
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
        int r = radius.get();

        // Step 1: Till dirt into farmland if enabled
        if (tillDirt.isEnabled()) {
            int hoeSlot = findHoeSlot();
            if (hoeSlot != -1) {
                for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
                    var block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH) {
                        // Only till if there's nothing on top
                        if (mc.world.getBlockState(pos.up()).isAir()) {
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
            }
        }

        // Step 2: Find empty farmland positions
        List<BlockPos> emptyFarmland = new ArrayList<>();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (mc.world.getBlockState(pos).getBlock() instanceof FarmlandBlock) {
                BlockPos above = pos.up();
                if (mc.world.getBlockState(above).isAir()) {
                    emptyFarmland.add(above.toImmutable());
                }
            }
        }

        if (emptyFarmland.isEmpty()) {
            timer.reset();
            return;
        }

        // Step 3: Find a seed in the hotbar
        int seedSlot = findSeedSlot();
        if (seedSlot == -1) {
            timer.reset();
            return;
        }

        // Plant on nearest empty farmland
        BlockPos target = emptyFarmland.stream()
                .min((a, b) -> Double.compare(a.getSquaredDistance(center), b.getSquaredDistance(center)))
                .orElse(null);
        if (target == null) {
            timer.reset();
            return;
        }

        // The farmland is one block below the target air slot
        BlockPos farmlandPos = target.down();
        int saved = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = seedSlot;

        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(farmlandPos).add(0, 0.5, 0), Direction.UP, farmlandPos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        // Apply bone meal if enabled
        if (bonemeal.isEnabled()) {
            int bonemealSlot = findBonemealSlot();
            if (bonemealSlot != -1) {
                mc.player.getInventory().selectedSlot = bonemealSlot;
                BlockHitResult bonemealHit = new BlockHitResult(
                        Vec3d.ofCenter(target).add(0, 0.5, 0), Direction.UP, target, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bonemealHit);
            }
        }

        mc.player.getInventory().selectedSlot = saved;
        timer.reset();
    }

    private int findHoeSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof HoeItem) return i;
        }
        return -1;
    }

    private int findSeedSlot() {
        if (mc.player == null) return -1;
        Item[] seeds = {Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS,
                Items.MELON_SEEDS, Items.PUMPKIN_SEEDS};
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            for (Item seed : seeds) {
                if (item == seed) return i;
            }
        }
        return -1;
    }

    private int findBonemealSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.BONE_MEAL) return i;
        }
        return -1;
    }
}
