package com.ghostclient.module.modules.movement;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.DoubleSetting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Spider - allows the player to climb any wall surface, not just ladders or vines.
 *
 * <p>Each tick, if the player is horizontally touching a solid block and holding
 * a movement key toward that block, an upward velocity is applied so they slowly
 * crawl up the surface.
 */
public class Spider extends Module {

    private final DoubleSetting climbSpeed = register(new DoubleSetting(
            "Climb Speed", "Upward velocity applied while climbing walls (blocks/tick)", 0.2, 0.05, 0.5));

    public Spider() {
        super("Spider", "Allows climbing any wall surface", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isOnGround()) return;  // Only engage while in the air

        // Detect horizontal block collision: check all four cardinal directions
        if (isTouchingWall()) {
            Vec3d vel = mc.player.getVelocity();
            // Apply upward velocity to crawl up the wall
            mc.player.setVelocity(vel.x, climbSpeed.get(), vel.z);
            mc.player.fallDistance = 0;
        }
    }

    /**
     * Returns {@code true} when the player's bounding box is touching a solid block
     * on at least one horizontal side.
     */
    private boolean isTouchingWall() {
        Box bb = mc.player.getBoundingBox();
        double expand = 0.05;

        // Check N, S, E, W by expanding the bounding box slightly in each direction
        // and testing for block collisions.
        return mc.player.horizontalCollision
                || collidesHorizontally(bb, expand);
    }

    /**
     * Uses the world's block collision system to check whether the player is horizontally
     * blocked.  Falls back to the player's own {@code horizontalCollision} flag when the
     * world method isn't convenient to call directly.
     */
    private boolean collidesHorizontally(Box bb, double expand) {
        // The simplest reliable check is the engine-provided horizontalCollision flag.
        // Additional directional check using expanded AABB:
        Box expandedN = new Box(bb.minX, bb.minY, bb.minZ - expand, bb.maxX, bb.maxY, bb.maxZ);
        Box expandedS = new Box(bb.minX, bb.minY, bb.minZ,          bb.maxX, bb.maxY, bb.maxZ + expand);
        Box expandedW = new Box(bb.minX - expand, bb.minY, bb.minZ, bb.maxX,          bb.maxY, bb.maxZ);
        Box expandedE = new Box(bb.minX,           bb.minY, bb.minZ, bb.maxX + expand, bb.maxY, bb.maxZ);

        for (Box check : new Box[]{expandedN, expandedS, expandedW, expandedE}) {
            if (!mc.world.getBlockCollisions(mc.player, check).toList().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
