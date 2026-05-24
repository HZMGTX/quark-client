package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * SafeWalk - prevents the player from walking off the edge of blocks by cancelling
 * horizontal movement when the resulting position would place the player above an air block.
 *
 * <p>Works by checking whether the block directly below the player's expected next
 * position (after applying the current velocity vector) is solid.  If not, the
 * horizontal movement component is zeroed out in the {@link EventMove} handler.
 */
public class SafeWalk extends Module {

    public SafeWalk() {
        super("SafeWalk", "Prevents walking off the edges of blocks", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null || mc.world == null) return;

        // Only engage when on the ground and actually moving horizontally
        if (!mc.player.isOnGround()) return;
        if (event.getX() == 0 && event.getZ() == 0) return;

        double nextX = mc.player.getX() + event.getX();
        double nextZ = mc.player.getZ() + event.getZ();
        double currentY = mc.player.getY();

        // Check the block directly below the projected next position
        BlockPos belowNext = new BlockPos(
                (int) Math.floor(nextX),
                (int) Math.floor(currentY) - 1,
                (int) Math.floor(nextZ));

        if (mc.world.getBlockState(belowNext).isAir()) {
            // Next step would go over an edge - cancel horizontal movement
            event.setX(0);
            event.setZ(0);
        }
    }
}
