package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * AutoFarm - automatically harvests mature crops and replants them.
 *
 * Supported crops:
 *   Wheat, Carrots, Potatoes, Beetroot (age-based, max age = 7 or 3)
 *   Melon/Pumpkin stems (breaks the fruit block next to the stem)
 */
public class AutoFarm extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Scan radius for crops", 5, 1, 10));

    private int tickDelay = 0;

    public AutoFarm() {
        super("AutoFarm", "Auto-harvests and replants crops", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        tickDelay++;
        if (tickDelay < 4) return;
        tickDelay = 0;

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();

        List<BlockPos> matureCrops = new ArrayList<>();

        for (int x = -r; x <= r; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.add(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (isMatureCrop(state, block)) {
                        matureCrops.add(pos);
                    }
                }
            }
        }

        if (matureCrops.isEmpty()) return;

        // Harvest the nearest mature crop
        BlockPos target = matureCrops.stream()
                .min((a, b) -> (int)(a.getSquaredDistance(center.getX(), center.getY(), center.getZ())
                        - b.getSquaredDistance(center.getX(), center.getY(), center.getZ())))
                .orElse(null);

        if (target == null) return;

        BlockState targetState = mc.world.getBlockState(target);
        Block targetBlock = targetState.getBlock();

        // Determine seed to replant
        Item seedItem = getSeed(targetBlock);
        boolean hasSeed = seedItem != null && hasSeedInInventory(seedItem);

        // Break the crop
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target, Direction.UP));
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target, Direction.UP));
        mc.world.breakBlock(target, true, mc.player);

        // Replant if we have seeds and the block below is farmland
        if (hasSeed) {
            BlockPos farmland = target.down();
            BlockState farmlandState = mc.world.getBlockState(farmland);
            if (farmlandState.getBlock() == Blocks.FARMLAND) {
                // Switch to seed
                int seedSlot = findSeedSlot(seedItem);
                if (seedSlot != -1) {
                    int savedSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = seedSlot;

                    Vec3d hitVec = Vec3d.ofCenter(farmland).add(0, 0.5, 0);
                    BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, farmland, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

                    mc.player.getInventory().selectedSlot = savedSlot;
                }
            }
        }
    }

    private boolean isMatureCrop(BlockState state, Block block) {
        if (block instanceof CropBlock crop) {
            return crop.isMature(state);
        }
        if (block == Blocks.MELON || block == Blocks.PUMPKIN) {
            return true; // Fruit blocks can always be harvested
        }
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
        // Check hotbar first
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == seed) return i;
        }
        // Check main inventory
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == seed) return i;
        }
        return -1;
    }
}
