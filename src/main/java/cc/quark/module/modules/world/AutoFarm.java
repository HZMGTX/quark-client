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
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class AutoFarm extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Crop", "Which crop types to farm",
            "All",
            "All", "Wheat", "Carrot", "Potato", "Beetroot"));

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Scan radius for crops", 5, 3, 10));

    private final BoolSetting replant = register(new BoolSetting(
            "Replant", "Replant seeds after harvesting", true));

    private final BoolSetting tendFarmland = register(new BoolSetting(
            "Tend Farmland", "Right-click dirt near crops to create farmland", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoFarm() {
        super("AutoFarm", "Auto-harvests and replants crops in a radius", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(50)) return;
        timer.reset();

        BlockPos center = mc.player.getBlockPos();
        int r = radius.get();

        if (tendFarmland.isEnabled()) {
            int hoeSlot = findHoeSlot();
            if (hoeSlot != -1) {
                for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
                    Block b = mc.world.getBlockState(pos).getBlock();
                    if (b != Blocks.DIRT && b != Blocks.GRASS_BLOCK && b != Blocks.DIRT_PATH) continue;
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.FARMLAND) continue;
                    Block aboveBlock = mc.world.getBlockState(pos.up()).getBlock();
                    if (!(aboveBlock instanceof CropBlock)) continue;
                    int saved = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = hoeSlot;
                    BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.player.getInventory().selectedSlot = saved;
                    return;
                }
            }
        }

        List<BlockPos> matureCrops = new ArrayList<>();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            BlockState state = mc.world.getBlockState(pos);
            Block block = state.getBlock();
            if (isMatureCrop(state, block)) matureCrops.add(pos.toImmutable());
        }

        if (matureCrops.isEmpty()) return;

        BlockPos target = matureCrops.stream()
                .min((a, b) -> Double.compare(a.getSquaredDistance(center), b.getSquaredDistance(center)))
                .orElse(null);
        if (target == null) return;

        Block targetBlock = mc.world.getBlockState(target).getBlock();
        Item seedItem = getSeed(targetBlock);
        boolean hasSeed = replant.isEnabled() && seedItem != null && findSeedSlot(seedItem) != -1;

        mc.interactionManager.attackBlock(target, Direction.UP);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (hasSeed) {
            BlockPos farmland = target.down();
            if (mc.world.getBlockState(farmland).getBlock() == Blocks.FARMLAND) {
                int seedSlot = findSeedSlot(seedItem);
                if (seedSlot != -1) {
                    int saved = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = seedSlot;
                    BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(farmland).add(0, 0.5, 0), Direction.UP, farmland, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                    mc.player.getInventory().selectedSlot = saved;
                }
            }
        }
    }

    private boolean isMatureCrop(BlockState state, Block block) {
        if (!isEnabledCrop(block)) return false;
        if (block instanceof CropBlock crop) return crop.isMature(state);
        return false;
    }

    private boolean isEnabledCrop(Block block) {
        String m = mode.get();
        if (m.equals("All")) return block == Blocks.WHEAT || block == Blocks.CARROTS || block == Blocks.POTATOES || block == Blocks.BEETROOTS;
        if (m.equals("Wheat"))    return block == Blocks.WHEAT;
        if (m.equals("Carrot"))   return block == Blocks.CARROTS;
        if (m.equals("Potato"))   return block == Blocks.POTATOES;
        if (m.equals("Beetroot")) return block == Blocks.BEETROOTS;
        return false;
    }

    private Item getSeed(Block block) {
        if (block == Blocks.WHEAT)     return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS)   return Items.CARROT;
        if (block == Blocks.POTATOES)  return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        return null;
    }

    private int findSeedSlot(Item seed) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == seed) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == seed) return i;
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
