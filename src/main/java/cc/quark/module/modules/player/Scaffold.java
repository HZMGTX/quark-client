package cc.quark.module.modules.player;

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

public class Scaffold extends Module {

    private final BoolSetting tower = register(new BoolSetting(
            "Tower", "Place blocks upward when holding jump (tower mode)", true));

    private final BoolSetting safeWalk = register(new BoolSetting(
            "Safe Walk", "Prevent walking off edges by stopping at block boundaries", true));

    private final BoolSetting safe = register(new BoolSetting(
            "Safe", "Don't walk toward edges without a block to place", false));

    private final IntSetting speedSetting = register(new IntSetting(
            "Speed", "Placement speed (blocks per tick)", 1, 1, 5));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between placements (0 = no delay)", 0, 0, 5));

    private int delayTicks = 0;

    public Scaffold() {
        super("Scaffold", "Auto-places blocks under player while walking in air", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        delayTicks = 0;
        if (mc.player != null) {
            if (safe.isEnabled()) {
                mc.options.sneakKey.setPressed(true);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.sneakKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Safe walk: if no block below the next step, stop horizontal movement at edge
        if (safeWalk.isEnabled() && mc.player.isOnGround()) {
            BlockPos belowNext = getPositionAhead().down();
            if (mc.world.getBlockState(belowNext).isAir()) {
                int blockSlotCheck = findBlockInHotbar();
                if (blockSlotCheck == -1) {
                    // No blocks to place, stop at edge
                    mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
                }
            }
        }

        // Delay between placements
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        int blockSlot = findBlockInHotbar();
        if (blockSlot == -1) return;

        int savedSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        if (tower.isEnabled() && mc.options.jumpKey.isPressed()) {
            // Tower mode: place block directly below and jump
            if (placeBelow()) {
                mc.player.jump();
                if (delay.get() > 0) delayTicks = delay.get();
            }
        } else {
            for (int i = 0; i < speedSetting.get(); i++) {
                if (!placeBelow()) break;
                if (delay.get() > 0) {
                    delayTicks = delay.get();
                    break;
                }
            }
        }

        mc.player.getInventory().selectedSlot = savedSlot;
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
                    Vec3d.of(placeDir.getVector()).multiply(0.5)
            );

            float targetYaw = getYawForDirection(dir);
            float targetPitch = (dir == Direction.DOWN) ? 89f : 45f;
            float oldYaw = mc.player.getYaw();
            float oldPitch = mc.player.getPitch();

            mc.player.setYaw(targetYaw);
            mc.player.setPitch(targetPitch);

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

    private BlockPos getPositionAhead() {
        if (mc.player == null) return BlockPos.ORIGIN;
        double yaw = Math.toRadians(mc.player.getYaw());
        double x = mc.player.getX() - Math.sin(yaw) * 0.6;
        double z = mc.player.getZ() + Math.cos(yaw) * 0.6;
        return new BlockPos((int) Math.floor(x), (int) Math.floor(mc.player.getY()), (int) Math.floor(z));
    }

    private int findBlockInHotbar() {
        if (mc.player == null) return -1;

        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                if (!(blockItem.getBlock() instanceof FallingBlock)) {
                    return i;
                }
                if (bestSlot == -1) bestSlot = i;
            }
        }
        return bestSlot;
    }

    private float getYawForDirection(Direction dir) {
        return switch (dir) {
            case NORTH -> 0f;
            case SOUTH -> 180f;
            case WEST -> -90f;
            case EAST -> 90f;
            default -> mc.player != null ? mc.player.getYaw() : 0f;
        };
    }
}
