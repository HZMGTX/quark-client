package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * AntiSlip - prevents the player from slipping on ice and slime by clamping
 * horizontal velocity to normal walking speed when on slippery surfaces.
 *
 * Vanilla ice slipperiness is 0.98 (normal ground = 0.6), so this module
 * resets horizontal velocity to controlled values on those blocks.
 */
public class AntiSlip extends Module {

    /** Normal ground friction factor (non-slippery). */
    private static final double NORMAL_FRICTION = 0.6;

    public AntiSlip() {
        super("AntiSlip", "Prevents slipping on ice/slime", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        BlockPos below = mc.player.getBlockPos().down();
        Block block = mc.world.getBlockState(below).getBlock();

        boolean slippery = (block == Blocks.ICE || block == Blocks.PACKED_ICE
                || block == Blocks.BLUE_ICE || block == Blocks.FROSTED_ICE
                || block == Blocks.SLIME_BLOCK);

        if (!slippery) return;

        // Clamp horizontal velocity as if the surface were normal ground
        Vec3d vel = mc.player.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        // Normal walking speed cap (sprint is ~0.29 b/t, walk ~0.215 b/t)
        double cap = 0.3;
        if (hSpeed > cap) {
            double scale = cap / hSpeed;
            mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
        }
    }
}
