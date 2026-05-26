package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
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

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Scan radius for crops", 5.0, 2.0, 8.0));

    private final BoolSetting replant = register(new BoolSetting(
            "Replant", "Replant seeds after harvesting", true));

    private final BoolSetting wheat = register(new BoolSetting(
            "Wheat", "Harvest wheat", true));

    private final BoolSetting carrots = register(new BoolSetting(
            "Carrots", "Harvest carrots", true));

    private final BoolSetting potatoes = register(new BoolSetting(
            "Potatoes", "Harvest potatoes", true));

    private final BoolSetting beetroot = register(new BoolSetting(
            "Beetroot", "Harvest beetroot", true));

    private final BoolSetting melon = register(new BoolSetting(
            "Melon", "Harvest melons", true));

    private final BoolSetting pumpkin = register(new BoolSetting(
            "Pumpkin", "Harvest pumpkins", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between harvests", 2, 0, 10));

    private int tickCounter = 0;

    public AutoFarm() {
        super("AutoFarm", "Auto-harvests and replants crops", Category.WORLD);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (tickCounter < delay.get()) {
            tickCounter++;
            return;
        }
        tickCounter = 0;

        BlockPos center = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());
        double rSq = range.get() * range.get();
        List<BlockPos> matureCrops = new ArrayList<>();

        for (int x = -r; x <= r; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (pos.getSquaredDistance(center) > rSq) continue;
                    BlockState state = mc.world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (isMatureCrop(state, block)) {
                        matureCrops.add(pos);
                    }
                }
            }
        }

        if (matureCrops.isEmpty()) return;

        BlockPos target = matureCrops.stream()
                .min((a, b) -> Double.compare(
                        a.getSquaredDistance(center),
                        b.getSquaredDistance(center)))
                .orElse(null);

        if (target == null) return;

        BlockState targetState = mc.world.getBlockState(target);
        Block targetBlock = targetState.getBlock();

        Item seedItem = getSeed(targetBlock);
        boolean hasSeed = replant.isEnabled() && seedItem != null && hasSeedInInventory(seedItem);

        mc.interactionManager.attackBlock(target, Direction.UP);

        if (hasSeed) {
            BlockPos farmland = target.down();
            BlockState farmlandState = mc.world.getBlockState(farmland);
            if (farmlandState.getBlock() == Blocks.FARMLAND) {
                int seedSlot = findSeedSlot(seedItem);
                if (seedSlot != -1) {
                    int savedSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = seedSlot < 9 ? seedSlot : savedSlot;
                    Vec3d hitVec = Vec3d.ofCenter(farmland).add(0, 0.5, 0);
                    BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, farmland, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                    mc.player.getInventory().selectedSlot = savedSlot;
                }
            }
        }
    }

    private boolean isMatureCrop(BlockState state, Block block) {
        if (!isEnabledCrop(block)) return false;
        if (block instanceof CropBlock crop) {
            return crop.isMature(state);
        }
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            return true;
        }
        return false;
    }

    private boolean isEnabledCrop(Block block) {
        if (block == Blocks.WHEAT) return wheat.isEnabled();
        if (block == Blocks.CARROTS) return carrots.isEnabled();
        if (block == Blocks.POTATOES) return potatoes.isEnabled();
        if (block == Blocks.BEETROOTS) return beetroot.isEnabled();
        if (block == Blocks.MELON) return melon.isEnabled();
        if (block == Blocks.PUMPKIN) return pumpkin.isEnabled();
        return false;
    }

    private Item getSeed(Block block) {
        if (block == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS) return Items.CARROT;
        if (block == Blocks.POTATOES) return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        if (block == Blocks.MELON) return Items.MELON_SEEDS;
        if (block == Blocks.PUMPKIN) return Items.PUMPKIN_SEEDS;
        return null;
    }

    private boolean hasSeedInInventory(Item seed) {
        return findSeedSlot(seed) != -1;
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
}
