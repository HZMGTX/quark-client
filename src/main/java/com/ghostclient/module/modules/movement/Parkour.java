package com.ghostclient.module.modules.movement;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import net.minecraft.util.math.BlockPos;

/**
 * Parkour - automatically jumps when the player reaches the edge of a block while
 * moving, preventing them from falling and enabling fluid parkour without pressing
 * the jump key manually.
 *
 * <p>Jump condition: the player is on the ground, moving horizontally, not sneaking,
 * and the block directly in front-below them (the block in their movement direction)
 * is air (i.e., they are at an edge).
 */
public class Parkour extends Module {

    public Parkour() {
        super("Parkour", "Automatically jumps at the edge of blocks", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isSneaking()) return;

        // Player must be actively moving
        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        // Check if the block in front of the player (in their velocity direction) is missing below
        double vx = mc.player.getVelocity().x;
        double vz = mc.player.getVelocity().z;

        if (Math.abs(vx) < 0.01 && Math.abs(vz) < 0.01) {
            // Use yaw-based direction instead if velocity is tiny
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            vx = -Math.sin(yaw) * 0.1;
            vz =  Math.cos(yaw) * 0.1;
        }

        // Step 1.5 blocks ahead in the movement direction
        double checkX = mc.player.getX() + vx * 6;
        double checkZ = mc.player.getZ() + vz * 6;
        double checkY = mc.player.getY();

        BlockPos aheadBelow = new BlockPos(
                (int) Math.floor(checkX),
                (int) Math.floor(checkY) - 1,
                (int) Math.floor(checkZ));

        boolean edgeAhead = mc.world.getBlockState(aheadBelow).isAir();

        if (edgeAhead) {
            mc.player.jump();
        }
    }
}
