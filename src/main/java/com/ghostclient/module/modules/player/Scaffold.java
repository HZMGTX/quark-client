package com.ghostclient.module.modules.player;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.IntSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Scaffold - automatically places blocks under the player while walking in the air.
 */
public class Scaffold extends Module {

    private final BoolSetting tower = register(new BoolSetting(
            "Tower", "Place blocks upward when holding jump (tower mode)", true));

    private final BoolSetting safe = register(new BoolSetting(
            "Safe", "Don't walk toward edges without a block to place", false));

    private final IntSetting speedSetting = register(new IntSetting(
            "Speed", "Placement speed (blocks per tick)", 1, 1, 5));

    public Scaffold() {
        super("Scaffold", "Auto-places blocks under player while walking in air", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            // Sneak to avoid falling off edges
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

        // Find block slot in hotbar
        int blockSlot = findBlockInHotbar();
        if (blockSlot == -1) return;

        int savedSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        if (tower.isEnabled() && mc.options.jumpKey.isPressed()) {
            // Tower mode: place block directly below and jump
            placeBelow();
            mc.player.jump();
        } else {
            // Normal scaffold: place block at position below player
            for (int i = 0; i < speedSetting.get(); i++) {
                if (!placeBelow()) break;
            }
        }

        mc.player.getInventory().selectedSlot = savedSlot;
    }

    /**
     * Attempts to place a block at the position below the player.
     * Returns true if a block was placed.
     */
    private boolean placeBelow() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return false;

        BlockPos pos = mc.player.getBlockPos().down();

        // Check if there's already a block there
        if (!mc.world.getBlockState(pos).isAir()) return false;

        // Find a solid adjacent block to place against
        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            BlockState neighborState = mc.world.getBlockState(neighborPos);

            if (neighborState.isAir() || neighborState.getBlock() instanceof FallingBlock) continue;

            // Calculate hit vector on the face of the neighbor block
            Direction placeDir = dir.getOpposite();
            Vec3d hitVec = Vec3d.ofCenter(neighborPos).add(
                    Vec3d.of(placeDir.getVector()).multiply(0.5)
            );

            // Temporarily rotate player to face placement direction
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

    /**
     * Finds the first hotbar slot containing a placeable block item.
     * Prefers non-falling blocks (sand/gravel ignored).
     */
    private int findBlockInHotbar() {
        if (mc.player == null) return -1;

        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem blockItem) {
                // Prefer non-falling blocks
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
