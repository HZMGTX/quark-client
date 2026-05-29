package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Honey - cancels the sticky slowdown applied by honey blocks.
 *
 * <p>Honey blocks cap horizontal velocity to 0.4× and prevent jumping.
 * This module restores normal speed by overriding the velocity multiplier
 * applied in the move event and re-enabling jumps.
 */
public class Honey extends Module {

    private final BoolSetting allowJump = register(new BoolSetting(
            "Allow Jump", "Allow jumping off honey blocks (normally prevented)", true));

    public Honey() {
        super("Honey", "Cancel honey block slowdown and sticky jump-prevention", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null || mc.world == null) return;
        if (!isOnHoney()) return;

        // Honey normally limits horizontal speed to ~40% of normal.
        // Compensate by multiplying back up.  Exact vanilla factor is 0.4;
        // we counteract it with 1/0.4 = 2.5.
        double compensate = 2.5;
        event.setX(event.getX() * compensate);
        event.setZ(event.getZ() * compensate);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!isOnHoney()) return;

        // Boost velocity directly in case the move event approach is insufficient
        Vec3d v = mc.player.getVelocity();
        double hLen = Math.sqrt(v.x * v.x + v.z * v.z);
        if (hLen > 0 && hLen < 0.1) {
            // Still too slow — give a gentle push
            mc.player.setVelocity(v.x * 2.0, v.y, v.z * 2.0);
        }
    }

    private boolean isOnHoney() {
        BlockPos pos = mc.player.getBlockPos();
        return mc.world.getBlockState(pos).isOf(Blocks.HONEY_BLOCK)
                || mc.world.getBlockState(pos.down()).isOf(Blocks.HONEY_BLOCK);
    }
}
