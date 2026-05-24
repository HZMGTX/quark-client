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
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Burrow - the classic bedrock/obsidian burrow exploit.
 *
 * <p>Attempts to place a block inside the player's own position so the server
 * clips the player into the block, making them largely immune to explosions
 * (crystal damage is massively reduced when burrowed into a solid block).
 *
 * <p>Technique:
 * <ol>
 *   <li>Snap the player to the exact centre of the foot block.</li>
 *   <li>Send a fake position packet placing the player 0.42 blocks above the block
 *       being placed (vanilla "step" trick to satisfy server-side on-ground checks).</li>
 *   <li>Place obsidian (or best available block) at foot level using the block below
 *       as the support face.</li>
 * </ol>
 */
public class Burrow extends Module {

    private final BoolSetting confirmBurrow = register(new BoolSetting(
            "Confirm Burrow", "Send an extra position packet to confirm the burrow position", true));

    /** Whether we have already executed the burrow this enable cycle. */
    private boolean burrowed = false;

    public Burrow() {
        super("Burrow", "Places a block in your position to clip into it (bedrock burrow exploit)",
                Category.COMBAT);
    }

    @Override
    public void onEnable() {
        burrowed = false;
    }

    @Override
    public void onDisable() {
        burrowed = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Only burrow once per enable cycle
        if (burrowed) return;

        // Must be on the ground
        if (!mc.player.isOnGround()) return;

        // Find a block to place
        int blockSlot = findObsidianSlot(mc);
        if (blockSlot == -1) return;

        BlockPos feet = mc.player.getBlockPos();

        // The block below feet is the support face
        BlockPos below = feet.down();
        BlockState belowState = mc.world.getBlockState(below);
        if (belowState.isAir()) return; // need solid ground

        // The feet block must currently be air/replaceable for placement
        BlockState feetState = mc.world.getBlockState(feet);
        if (!feetState.isAir() && !feetState.isReplaceable()) {
            // Already burrowed
            burrowed = true;
            return;
        }

        // Step 1: Snap to block centre
        double centreX = feet.getX() + 0.5;
        double centreZ = feet.getZ() + 0.5;
        mc.player.setPosition(centreX, mc.player.getY(), centreZ);

        // Step 2: Switch to the block slot
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        // Step 3: Send fake offset position packet to satisfy server on-ground check
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            centreX, mc.player.getY() + 0.42, centreZ, false));
        }

        // Step 4: Place block at feet position using the DOWN face of the feet block
        // (i.e., place on top of the below block)
        Vec3d hitVec = new Vec3d(centreX, feet.getY(), centreZ);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, below, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        // Step 5: Optionally confirm position
        if (confirmBurrow.isEnabled() && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            centreX, mc.player.getY(), centreZ, true));
        }

        // Restore slot
        mc.player.getInventory().selectedSlot = prevSlot;

        burrowed = true;
    }

    /**
     * Returns the hotbar slot index of obsidian, or any solid block item if obsidian is absent.
     * Returns -1 if no suitable block is found.
     */
    private int findObsidianSlot(MinecraftClient mc) {
        int fallback = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof BlockItem)) continue;

            if (stack.getItem() == Items.OBSIDIAN || stack.getItem() == Items.BEDROCK) return i;
            if (fallback == -1) fallback = i;
        }

        return fallback;
    }
}
