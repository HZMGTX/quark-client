package com.ghostclient.module.modules.combat;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
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
 * Surround - places obsidian (or any available solid block) at the 8 positions
 * surrounding the player's feet to protect against end crystal explosions.
 *
 * <p>Cardinal positions (N/S/E/W) are placed first; corners (NE/NW/SE/SW) second.
 * A block is only placed when the target position is air/replaceable and there is
 * a valid adjacent solid face to place against.
 */
public class Surround extends Module {

    private final BoolSetting center = register(new BoolSetting(
            "Center", "Snap player to the centre of the current block before placing", true));

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "Only On Ground", "Only place blocks while the player is standing on the ground", true));

    public Surround() {
        super("Surround", "Places blocks around your feet to protect against crystals", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (center.isEnabled()) {
            // Snap to block centre
            BlockPos feet = mc.player.getBlockPos();
            double centreX = feet.getX() + 0.5;
            double centreZ = feet.getZ() + 0.5;
            mc.player.setPosition(centreX, mc.player.getY(), centreZ);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;

        // Surround positions relative to player feet (dx, dz)
        int[][] offsets = {
            {  0,  1 },  // North
            {  0, -1 },  // South
            {  1,  0 },  // East
            { -1,  0 },  // West
            {  1,  1 },  // NE
            { -1,  1 },  // NW
            {  1, -1 },  // SE
            { -1, -1 }   // SW
        };

        BlockPos feet = mc.player.getBlockPos();

        // Find a solid block in hotbar to place (prefer obsidian, then any block)
        int blockSlot = findBestBlockSlot(mc);
        if (blockSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        for (int[] off : offsets) {
            BlockPos target = feet.add(off[0], 0, off[1]);
            BlockState existing = mc.world.getBlockState(target);
            if (!existing.isAir() && !existing.isReplaceable()) continue;

            // Find a neighbour face to place against
            Direction placeDir = findSupportFace(mc, target);
            if (placeDir == null) continue;

            BlockPos neighbor = target.offset(placeDir);
            Vec3d hitVec = Vec3d.ofCenter(target).add(
                    placeDir.getOffsetX() * 0.5,
                    placeDir.getOffsetY() * 0.5,
                    placeDir.getOffsetZ() * 0.5);

            BlockHitResult hitResult = new BlockHitResult(hitVec, placeDir.getOpposite(), neighbor, false);

            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    /**
     * Returns the direction from {@code target} toward the best adjacent solid block
     * that can serve as a placement face, or {@code null} if none found.
     */
    private Direction findSupportFace(MinecraftClient mc, BlockPos target) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (!state.isAir() && !state.isReplaceable() && state.isSolidBlock(mc.world, neighbor)) {
                return dir;
            }
        }
        return null;
    }

    /**
     * Finds the hotbar slot with the best block for surrounding.
     * Prefers obsidian, then any block item.
     * Returns -1 if no suitable block is found.
     */
    private int findBestBlockSlot(MinecraftClient mc) {
        int fallback = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof BlockItem)) continue;

            if (stack.getItem() == Items.OBSIDIAN) return i;
            if (fallback == -1) fallback = i;
        }

        return fallback;
    }
}
