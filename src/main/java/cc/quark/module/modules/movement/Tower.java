package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Tower extends Module {

    private final BoolSetting autoJump = register(new BoolSetting(
            "Auto Jump", "Automatically jump while placing blocks below", true));

    private final IntSetting speed = register(new IntSetting(
            "Speed", "Ticks between block placements (0 = every tick)", 0, 0, 5));

    private int delayTicks = 0;

    public Tower() {
        super("Tower", "Builds a tower by auto-jumping and placing blocks below", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        delayTicks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        boolean jumpHeld = mc.options.jumpKey.isPressed();
        if (!jumpHeld) return;

        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        int blockSlot = findSolidBlockInHotbar();
        if (blockSlot == -1) return;

        int savedSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        boolean placed = placeBelow();

        mc.player.getInventory().selectedSlot = savedSlot;

        if (placed) {
            if (autoJump.isEnabled()) {
                mc.player.jump();
            }
            if (speed.get() > 0) {
                delayTicks = speed.get();
            }
        }
    }

    private boolean placeBelow() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return false;

        BlockPos pos = mc.player.getBlockPos().down();

        if (!mc.world.getBlockState(pos).isAir()) return false;

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            BlockState neighborState = mc.world.getBlockState(neighborPos);

            if (neighborState.isAir() || neighborState.getBlock() instanceof FallingBlock) continue;

            Direction placeDir = dir.getOpposite();
            Vec3d hitVec = Vec3d.ofCenter(neighborPos).add(
                    Vec3d.of(placeDir.getVector()).multiply(0.5));

            float oldYaw = mc.player.getYaw();
            float oldPitch = mc.player.getPitch();

            mc.player.setPitch(89.9f);

            BlockHitResult hitResult = new BlockHitResult(hitVec, placeDir, neighborPos, false);
            ActionResult result = mc.interactionManager.interactBlock(
                    mc.player, Hand.MAIN_HAND, hitResult);

            mc.player.setYaw(oldYaw);
            mc.player.setPitch(oldPitch);

            if (result.isAccepted()) {
                mc.player.swingHand(Hand.MAIN_HAND);
                return true;
            }
        }

        return false;
    }

    private int findSolidBlockInHotbar() {
        if (mc.player == null) return -1;
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof BlockItem blockItem)) continue;
            if (!(blockItem.getBlock() instanceof FallingBlock)) return i;
            if (bestSlot == -1) bestSlot = i;
        }
        return bestSlot;
    }
}
