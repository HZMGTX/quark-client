package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.block.BlockState;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * FarmHelper - Assists with harvesting and replanting crops automatically.
 * Handles wheat, carrots, potatoes, beetroots, nether wart, melon, and pumpkin.
 * Supports hoe-tilling nearby dirt, replanting after harvest, and bone-mealing young crops.
 */
public class FarmHelper extends Module {

    private final ModeSetting crop = register(new ModeSetting(
            "Crop", "Crop type to target",
            "All",
            "All", "Wheat", "Carrot", "Potato", "Beetroot", "NetherWart", "Melon", "Pumpkin"));
    private final IntSetting radius = register(new IntSetting(
            "Radius", "Harvest radius around player", 5, 2, 10));
    private final BoolSetting replant = register(new BoolSetting(
            "Replant", "Replant seeds after harvesting", true));
    private final BoolSetting boneMeal = register(new BoolSetting(
            "BoneMeal", "Apply bone meal to young crops", false));
    private final BoolSetting tillDirt = register(new BoolSetting(
            "TillDirt", "Use hoe to till adjacent dirt/grass into farmland", false));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between actions", 50, 10, 500));

    private final TimerUtil timer = new TimerUtil();

    public FarmHelper() {
        super("FarmHelper", "Assists with harvesting and replanting crops automatically", Category.WORLD);
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

        // Phase 1: Till nearby dirt into farmland
        if (tillDirt.isEnabled()) {
            int hoeSlot = findHoeSlot();
            if (hoeSlot != -1) {
                for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
                    Block b = mc.world.getBlockState(pos).getBlock();
                    if (b != Blocks.DIRT && b != Blocks.GRASS_BLOCK) continue;
                    Block above = mc.world.getBlockState(pos.up()).getBlock();
                    if (above instanceof CropBlock || above instanceof NetherWartBlock) continue;
                    // Only till if there's a crop above a nearby block
                    if (!hasNearbyCrop(pos)) continue;
                    int prev = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = hoeSlot;
                    BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    mc.player.getInventory().selectedSlot = prev;
                    timer.reset();
                    return;
                }
            }
        }

        // Phase 2: Harvest mature crops
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            BlockState state = mc.world.getBlockState(pos);
            Block block = state.getBlock();
            if (!isEnabledCrop(block)) continue;
            if (!isMature(state, block)) continue;

            BlockPos immutable = pos.toImmutable();
            Item seed = getSeed(block);
            boolean canReplant = replant.isEnabled() && seed != null;

            // Break crop
            mc.interactionManager.attackBlock(immutable, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Replant
            if (canReplant) {
                BlockPos soil = immutable.down();
                Block soilBlock = mc.world.getBlockState(soil).getBlock();
                if (soilBlock == Blocks.FARMLAND || soilBlock == Blocks.SOUL_SAND) {
                    int seedSlot = findItemSlot(seed);
                    if (seedSlot != -1) {
                        int prev = mc.player.getInventory().selectedSlot;
                        mc.player.getInventory().selectedSlot = seedSlot;
                        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(soil).add(0, 0.5, 0), Direction.UP, soil.toImmutable(), false);
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                        mc.player.getInventory().selectedSlot = prev;
                    }
                }
            }

            timer.reset();
            return;
        }

        // Phase 3: Bone meal young crops
        if (boneMeal.isEnabled()) {
            int boneSlot = findItemSlot(Items.BONE_MEAL);
            if (boneSlot != -1) {
                for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
                    BlockState state = mc.world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (!isEnabledCrop(block)) continue;
                    if (isMature(state, block)) continue; // skip mature
                    BlockPos immutable = pos.toImmutable();
                    int prev = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = boneSlot;
                    BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(immutable).add(0, 0.5, 0), Direction.UP, immutable, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    mc.player.getInventory().selectedSlot = prev;
                    timer.reset();
                    return;
                }
            }
        }
    }

    private boolean isEnabledCrop(Block block) {
        String m = crop.get();
        if (m.equals("All")) return isCropBlock(block);
        if (m.equals("Wheat"))      return block == Blocks.WHEAT;
        if (m.equals("Carrot"))     return block == Blocks.CARROTS;
        if (m.equals("Potato"))     return block == Blocks.POTATOES;
        if (m.equals("Beetroot"))   return block == Blocks.BEETROOTS;
        if (m.equals("NetherWart")) return block == Blocks.NETHER_WART;
        if (m.equals("Melon"))      return block == Blocks.MELON;
        if (m.equals("Pumpkin"))    return block == Blocks.PUMPKIN;
        return false;
    }

    private boolean isCropBlock(Block block) {
        return block == Blocks.WHEAT || block == Blocks.CARROTS || block == Blocks.POTATOES
                || block == Blocks.BEETROOTS || block == Blocks.NETHER_WART
                || block == Blocks.MELON || block == Blocks.PUMPKIN;
    }

    private boolean isMature(BlockState state, Block block) {
        if (block instanceof CropBlock crop) return crop.isMature(state);
        if (block instanceof NetherWartBlock) {
            return state.get(NetherWartBlock.AGE) == 3;
        }
        // Melons and pumpkins are always "mature" as full blocks
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) return true;
        return false;
    }

    private Item getSeed(Block block) {
        if (block == Blocks.WHEAT)      return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS)    return Items.CARROT;
        if (block == Blocks.POTATOES)   return Items.POTATO;
        if (block == Blocks.BEETROOTS)  return Items.BEETROOT_SEEDS;
        if (block == Blocks.NETHER_WART) return Items.NETHER_WART;
        return null; // Melons/pumpkins handled via stems
    }

    private boolean hasNearbyCrop(BlockPos pos) {
        for (BlockPos adj : BlockPos.iterate(pos.add(-2, -1, -2), pos.add(2, 1, 2))) {
            Block b = mc.world.getBlockState(adj).getBlock();
            if (b instanceof CropBlock || b instanceof NetherWartBlock) return true;
        }
        return false;
    }

    private int findItemSlot(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    private int findHoeSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof HoeItem) return i;
        }
        return -1;
    }
}
