package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoTrap — places blocks at enemy feet to trap them.
 * Supports surrounding all 4 sides when the Surround setting is on.
 */
public class AutoTrap extends Module {

    private final BoolSetting surround = register(new BoolSetting(
            "Surround", "Trap all 4 sides around the target", true));

    private final IntSetting range = register(new IntSetting(
            "Range", "Maximum distance to target for trapping", 3, 1, 5));

    public AutoTrap() {
        super("AutoTrap", "Places blocks at enemy feet to trap them", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Find nearest enemy in range
        LivingEntity target = findNearestEnemy(range.get());
        if (target == null) return;

        // Find a block to place
        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        BlockPos targetFeet = target.getBlockPos();

        if (surround.isEnabled()) {
            // Place on all 4 cardinal sides at feet level
            int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (int[] off : offsets) {
                BlockPos pos = targetFeet.add(off[0], 0, off[1]);
                tryPlace(pos);
            }
        } else {
            // Just place at feet
            tryPlace(targetFeet);
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private boolean tryPlace(BlockPos pos) {
        if (mc.world == null || mc.interactionManager == null) return false;
        BlockState state = mc.world.getBlockState(pos);
        if (!state.isAir()) return false;

        Direction supportDir = findSupport(pos);
        if (supportDir == null) return false;

        BlockPos neighbor = pos.offset(supportDir);
        Vec3d hitVec = Vec3d.ofCenter(pos).add(
                supportDir.getOffsetX() * 0.5,
                supportDir.getOffsetY() * 0.5,
                supportDir.getOffsetZ() * 0.5);

        BlockHitResult hit = new BlockHitResult(hitVec, supportDir.getOpposite(), neighbor, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        return true;
    }

    private Direction findSupport(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (!state.isAir() && !state.isReplaceable() && state.isSolidBlock(mc.world, neighbor)) {
                return dir;
            }
        }
        return null;
    }

    private LivingEntity findNearestEnemy(int maxRange) {
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            if (entity instanceof LivingEntity living) {
                if (living.isDead() || living.getHealth() <= 0) continue;
                double dist = mc.player.distanceTo(living);
                if (dist <= maxRange && dist < closestDist) {
                    closestDist = dist;
                    closest = living;
                }
            }
        }
        return closest;
    }

    private int findBlockSlot() {
        int fallback = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) continue;
            if (stack.getItem() == Items.OBSIDIAN) return i;
            if (fallback == -1) fallback = i;
        }
        return fallback;
    }
}
