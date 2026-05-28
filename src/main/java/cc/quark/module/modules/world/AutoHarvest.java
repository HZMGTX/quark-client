package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoHarvest — harvests fully grown crops in range and optionally replants them.
 *
 * Supported crops:
 *   - Wheat / Carrots / Potatoes (age 7 via CropBlock.isMature())
 *   - Beetroots (age 3, also a CropBlock subclass)
 *   - Nether Wart (age 3, checked via Properties.AGE_3)
 *
 * Settings:
 *   Range   — block radius to scan.
 *   Replant — replant after harvesting (places seed from hotbar onto farmland/soul sand).
 *   Delay   — milliseconds between harvest/replant actions.
 */
public class AutoHarvest extends Module {

    private final DoubleSetting range   = register(new DoubleSetting("Range",   "Scan radius in blocks", 5.0, 2.0, 8.0));
    private final BoolSetting   replant = register(new BoolSetting("Replant",   "Automatically replant crops after harvesting.", true));
    private final IntSetting    delay   = register(new IntSetting("Delay",      "Milliseconds between actions", 100, 0, 500));

    private final TimerUtil timer = new TimerUtil();

    public AutoHarvest() {
        super("AutoHarvest", "Harvests fully grown crops nearby.", Category.WORLD);
    }

    @Override
    public void onEnable() { timer.reset(); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        timer.reset();

        int r = (int) Math.ceil(range.get());
        BlockPos center = mc.player.getBlockPos();

        BlockPos nearest     = null;
        double  closestDist  = Double.MAX_VALUE;
        double  rangeSq      = range.get() * range.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            double dist = pos.getSquaredDistance(mc.player.getPos());
            if (dist > rangeSq) continue;

            BlockState state = mc.world.getBlockState(pos);
            if (!isFullyGrown(state)) continue;

            if (dist < closestDist) {
                closestDist = dist;
                nearest     = pos.toImmutable();
            }
        }

        if (nearest != null) {
            harvestAndReplant(nearest);
        }
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    private void harvestAndReplant(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();

        // Break the crop
        mc.interactionManager.attackBlock(pos, Direction.UP);

        if (replant.isEnabled()) {
            Item seed = getSeedForBlock(block);
            if (seed == null) return;

            int seedSlot = findSeedInHotbar(seed);
            if (seedSlot == -1) return;

            // Replant onto the soil block below
            BlockPos soil = pos.down();
            boolean canReplant = mc.world.getBlockState(soil).isOf(Blocks.FARMLAND)
                    || mc.world.getBlockState(soil).isOf(Blocks.SOUL_SAND);
            if (!canReplant) return;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = seedSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(soil), Direction.UP, soil, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.getInventory().selectedSlot = prev;
        }
    }

    // -------------------------------------------------------------------------
    // Crop maturity checks
    // -------------------------------------------------------------------------

    private static boolean isFullyGrown(BlockState state) {
        Block block = state.getBlock();

        // Standard CropBlock handles wheat, carrots, potatoes, beetroots
        if (block instanceof CropBlock crop) {
            return crop.isMature(state);
        }

        // Nether wart — AGE_3 == 3 means fully grown
        if (block == Blocks.NETHER_WART) {
            return state.contains(Properties.AGE_3) && state.get(Properties.AGE_3) == 3;
        }

        return false;
    }

    // -------------------------------------------------------------------------
    // Seed helpers
    // -------------------------------------------------------------------------

    private static Item getSeedForBlock(Block block) {
        if (block == Blocks.WHEAT)        return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS)      return Items.CARROT;
        if (block == Blocks.POTATOES)     return Items.POTATO;
        if (block == Blocks.BEETROOTS)    return Items.BEETROOT_SEEDS;
        if (block == Blocks.NETHER_WART)  return Items.NETHER_WART;
        return null;
    }

    private int findSeedInHotbar(Item seed) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(seed)) return i;
        }
        return -1;
    }
}
