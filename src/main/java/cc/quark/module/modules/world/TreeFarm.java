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
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TreeFarm extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius to scan for logs and saplings", 6, 1, 12));
    private final BoolSetting replant = register(new BoolSetting(
            "Replant", "Replant saplings after chopping", true));
    private final BoolSetting boneMeal = register(new BoolSetting(
            "Bone Meal", "Use bone meal on freshly planted saplings", false));
    private final DoubleSetting breakDelay = register(new DoubleSetting(
            "Break Delay", "Delay between log breaks (ms)", 100, 50, 500));
    private final TimerUtil timer = new TimerUtil();

    private static final Set<Block> LOGS = new HashSet<>(Arrays.asList(
            Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG,
            Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG,
            Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG
    ));

    public TreeFarm() {
        super("TreeFarm", "Full tree farming with replanting and collection", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached((long) breakDelay.get())) return;

        int r = radius.get();
        BlockPos center = mc.player.getBlockPos();

        // Switch to axe if available
        int axeSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof AxeItem) {
                axeSlot = i;
                break;
            }
        }

        // Step 1: Chop any nearby logs
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, r + 6, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > r * r) continue;
            Block block = mc.world.getBlockState(pos).getBlock();
            if (!LOGS.contains(block)) continue;

            int saved = mc.player.getInventory().selectedSlot;
            if (axeSlot != -1) mc.player.getInventory().selectedSlot = axeSlot;

            mc.interactionManager.attackBlock(pos, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = saved;
            timer.reset();
            return;
        }

        // Step 2: Replant saplings on empty dirt/grass
        if (replant.isEnabled()) {
            for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, 0, r))) {
                if (pos.getSquaredDistance(mc.player.getPos()) > r * r) continue;
                Block groundBlock = mc.world.getBlockState(pos).getBlock();
                if (groundBlock != Blocks.DIRT && groundBlock != Blocks.GRASS_BLOCK) continue;
                if (!mc.world.getBlockState(pos.up()).isAir()) continue;

                Item sapling = getBestSapling();
                if (sapling == null) continue;

                int saplingSlot = findItem(sapling);
                if (saplingSlot == -1) continue;

                int saved = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = saplingSlot;
                BlockHitResult hit = new BlockHitResult(
                        Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = saved;
                timer.reset();

                // Optionally apply bone meal immediately
                if (boneMeal.isEnabled()) {
                    applyBoneMeal(pos.up());
                }
                return;
            }
        }

        timer.reset();
    }

    private void applyBoneMeal(BlockPos saplingPos) {
        int slot = findItem(Items.BONE_MEAL);
        if (slot == -1) return;
        int saved = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(saplingPos).add(0, 0.5, 0), Direction.UP, saplingPos.toImmutable(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = saved;
    }

    private Item getBestSapling() {
        Item[] saplings = {
            Items.OAK_SAPLING, Items.SPRUCE_SAPLING, Items.BIRCH_SAPLING,
            Items.JUNGLE_SAPLING, Items.ACACIA_SAPLING, Items.DARK_OAK_SAPLING
        };
        for (Item s : saplings) {
            if (findItem(s) != -1) return s;
        }
        return null;
    }

    private int findItem(Item item) {
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
