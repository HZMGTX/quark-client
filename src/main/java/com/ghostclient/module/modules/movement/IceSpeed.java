package com.ghostclient.module.modules.movement;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.DoubleSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * IceSpeed - maintains a controlled movement speed on ice and packed/blue ice by
 * counteracting the excessive acceleration that vanilla ice normally provides.
 *
 * <p>By default ice has slipperiness 0.98 (vanilla blocks = 0.6), causing the player
 * to slide uncontrollably.  This module clamps the horizontal velocity each tick to a
 * configurable maximum so movement remains precise.
 */
public class IceSpeed extends Module {

    private final DoubleSetting maxSpeed = register(new DoubleSetting(
            "Max Speed", "Maximum horizontal speed on ice (blocks/tick)", 0.35, 0.1, 2.0));

    public IceSpeed() {
        super("IceSpeed", "Maintains controlled speed on ice surfaces", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        // Check if the player is on an icy block
        BlockPos belowPos = mc.player.getBlockPos().down();
        Block below = mc.world.getBlockState(belowPos).getBlock();

        boolean onIce = (below == Blocks.ICE
                      || below == Blocks.PACKED_ICE
                      || below == Blocks.BLUE_ICE
                      || below == Blocks.FROSTED_ICE);

        if (!onIce) return;

        Vec3d vel = mc.player.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

        if (hSpeed > maxSpeed.get()) {
            double scale = maxSpeed.get() / hSpeed;
            mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
        }

        // Also boost the player slightly if they are actively pressing movement keys
        // so they don't decelerate too slowly on ice
        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;

        if (moving && hSpeed < 0.15) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            double nudgeX = -Math.sin(yaw) * 0.08;
            double nudgeZ =  Math.cos(yaw) * 0.08;
            mc.player.setVelocity(vel.x + nudgeX, vel.y, vel.z + nudgeZ);
        }
    }
}
