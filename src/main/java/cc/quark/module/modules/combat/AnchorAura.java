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
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AnchorAura - places and detonates respawn anchors in the Overworld/End to deal explosion damage.
 * Respawn anchors only explode outside the Nether.
 */
public class AnchorAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to find targets", 5.0, 2.0, 6.0));

    private final BoolSetting autoSwap = register(new BoolSetting(
            "Auto Swap", "Swap to a respawn anchor in hotbar when none is held", true));

    private final BoolSetting antiSuicide = register(new BoolSetting(
            "Anti Suicide", "Skip placement if self damage would be lethal", true));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between activations", 50, 0, 500));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;
    // Track placed anchor positions so we can detonate them
    private BlockPos pendingAnchorPos = null;
    private boolean waitingToDetonate = false;

    public AnchorAura() {
        super("AnchorAura", "Places and detonates respawn anchors to damage nearby players", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return String.format("%.1f", range.get());
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
        pendingAnchorPos = null;
        waitingToDetonate = false;
    }

    @Override
    public void onDisable() {
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
        if (target == null) {
            pendingAnchorPos = null;
            waitingToDetonate = false;
            return;
        }

        // If we placed an anchor previously, try to detonate it
        if (waitingToDetonate && pendingAnchorPos != null) {
            if (mc.world.getBlockState(pendingAnchorPos).getBlock() instanceof RespawnAnchorBlock) {
                // Need glowstone to charge it, then right-click again to detonate
                // For now, just right-click to attempt detonation
                Vec3d hitVec = Vec3d.ofCenter(pendingAnchorPos);
                BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pendingAnchorPos, false);
                // Switch to glowstone if available, else right-click to detonate with current item
                int glowstoneSlot = findGlowstoneSlot();
                int currentSlot = mc.player.getInventory().selectedSlot;
                if (glowstoneSlot != -1 && glowstoneSlot != currentSlot) {
                    mc.player.getInventory().selectedSlot = glowstoneSlot;
                }
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
                timer.reset();
                // Immediately attempt detonation (right-click again without glowstone)
                waitingToDetonate = false;
                pendingAnchorPos = null;
                restoreSlot();
                return;
            } else {
                pendingAnchorPos = null;
                waitingToDetonate = false;
            }
        }

        // Find anchor slot in hotbar
        int anchorSlot = findAnchorSlot();
        if (anchorSlot == -1) return;

        int currentSlot = mc.player.getInventory().selectedSlot;
        if (currentSlot != anchorSlot) {
            if (!autoSwap.isEnabled()) return;
            prevSlot = currentSlot;
            mc.player.getInventory().selectedSlot = anchorSlot;
        }

        BlockPos placePos = findPlacementPos(target);
        if (placePos == null) {
            restoreSlot();
            return;
        }

        // Anti-suicide check
        if (antiSuicide.isEnabled()) {
            Vec3d explosionPos = Vec3d.ofCenter(placePos);
            float selfDamage = DamageUtil.getSelfDamage(explosionPos);
            if (selfDamage >= mc.player.getHealth()) {
                restoreSlot();
                return;
            }
        }

        // Place the anchor
        Direction face = Direction.UP;
        Vec3d hitVec = Vec3d.ofCenter(placePos.down()).add(0, 0.5, 0);
        BlockHitResult hit = new BlockHitResult(hitVec, face, placePos.down(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        pendingAnchorPos = placePos;
        waitingToDetonate = true;
        timer.reset();
        restoreSlot();
    }

    private PlayerEntity findNearestTarget() {
        PlayerEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;
            if (player.isDead() || player.getHealth() <= 0) continue;
            double dist = mc.player.distanceTo(player);
            if (dist <= range.get() && dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }
        return nearest;
    }

    private int findAnchorSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.RESPAWN_ANCHOR) return i;
        }
        return -1;
    }

    private int findGlowstoneSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.GLOWSTONE) return i;
        }
        return -1;
    }

    private BlockPos findPlacementPos(PlayerEntity target) {
        BlockPos targetPos = target.getBlockPos();
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
