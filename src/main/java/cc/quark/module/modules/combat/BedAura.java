package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.DamageUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * BedAura - places and detonates beds in the Nether to deal explosion damage to nearby players.
 */
public class BedAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to find targets", 5.0, 2.0, 6.0));

    private final BoolSetting autoSwap = register(new BoolSetting(
            "Auto Swap", "Swap to a bed in hotbar when no bed is held", true));

    private final BoolSetting antiSuicide = register(new BoolSetting(
            "Anti Suicide", "Skip placement if self damage would be lethal", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between activations", 50, 0, 500));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public BedAura() {
        super("BedAura", "Places and detonates beds to damage nearby players", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return String.format("%.1f", range.get());
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        // Restore hotbar slot if we swapped
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Find nearest player target within range
        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        // Find a bed in the hotbar
        int bedSlot = findBedSlot();
        if (bedSlot == -1) return;

        // Swap to bed slot if needed
        int currentSlot = mc.player.getInventory().selectedSlot;
        if (currentSlot != bedSlot) {
            if (!autoSwap.isEnabled()) return;
            prevSlot = currentSlot;
            mc.player.getInventory().selectedSlot = bedSlot;
        }

        // Find a placement position near the target (1 block away from them)
        BlockPos placePos = findBedPlacementPos(target);
        if (placePos == null) {
            restoreSlot();
            return;
        }

        // Check for existing bed at the position — if so, interact to detonate
        if (mc.world.getBlockState(placePos).getBlock() instanceof BedBlock) {
            // Detonate by right-clicking the bed
            Vec3d hitVec = Vec3d.ofCenter(placePos);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, placePos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            restoreSlot();
            return;
        }

        // Anti-suicide check: estimate explosion damage at placement
        if (antiSuicide.isEnabled()) {
            Vec3d explosionPos = Vec3d.ofCenter(placePos);
            float selfDamage = DamageUtil.getSelfDamage(explosionPos);
            if (selfDamage >= mc.player.getHealth()) {
                restoreSlot();
                return;
            }
        }

        // Place the bed
        Direction face = Direction.UP;
        Vec3d hitVec = Vec3d.ofCenter(placePos.down()).add(0, 0.5, 0);
        BlockHitResult hit = new BlockHitResult(hitVec, face, placePos.down(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
        restoreSlot();
    }

    private PlayerEntity findNearestTarget() {
        PlayerEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;
            if (player.isRemoved() || player.getHealth() <= 0) continue;
            double dist = mc.player.distanceTo(player);
            if (dist <= range.get() && dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }
        return nearest;
    }

    private int findBedSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem __bi && __bi.getBlock() instanceof BedBlock) return i;
        }
        return -1;
    }

    private BlockPos findBedPlacementPos(PlayerEntity target) {
        BlockPos targetPos = target.getBlockPos();
        // Try positions adjacent to target (and at their feet level)
        Direction[] horizontals = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        for (Direction dir : horizontals) {
            BlockPos candidate = targetPos.offset(dir);
            if (mc.world.getBlockState(candidate).isAir() && mc.world.getBlockState(candidate.up()).isAir()) {
                double distToSelf = mc.player.getEyePos().distanceTo(Vec3d.ofCenter(candidate));
                if (distToSelf <= range.get() + 1.0) {
                    return candidate;
                }
            }
        }
        // Fallback: check if target's own position has a bed
        if (mc.world.getBlockState(targetPos).getBlock() instanceof BedBlock) {
            return targetPos;
        }
        // Place directly at target's feet
        if (mc.world.getBlockState(targetPos).isAir()) {
            return targetPos;
        }
        return null;
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
