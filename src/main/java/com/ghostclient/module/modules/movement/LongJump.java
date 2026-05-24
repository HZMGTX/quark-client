package com.ghostclient.module.modules.movement;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventJump;
import com.ghostclient.event.events.EventMove;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * LongJump - causes the player to jump much farther than normal by combining
 * a speed boost at jump initiation with sustained horizontal momentum in the air.
 *
 * <p>Strategy:
 * <ol>
 *   <li>At the moment of jump ({@link EventJump}), apply a forward burst of horizontal velocity.</li>
 *   <li>While in the air ({@link EventMove}), maintain that boosted velocity so drag doesn't
 *       reduce it too quickly.</li>
 * </ol>
 */
public class LongJump extends Module {

    private final DoubleSetting distance = register(new DoubleSetting(
            "Distance", "Horizontal distance multiplier compared to a normal jump", 3.0, 1.5, 8.0));

    /** Whether the player is currently in a boosted jump. */
    private boolean inBoostJump = false;
    /** Horizontal speed vector saved at jump moment for air-phase maintenance. */
    private double savedVx = 0;
    private double savedVz = 0;

    public LongJump() {
        super("LongJump", "Jumps much farther than normal", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        inBoostJump = false;
    }

    @EventHandler
    public void onJump(EventJump event) {
        if (mc.player == null) return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        // Vanilla sprint-jump horizontal speed is about 0.36 blocks/tick
        double boostSpeed = 0.36 * distance.get() * 0.4; // scaled to feel natural

        savedVx = -Math.sin(yaw) * boostSpeed;
        savedVz =  Math.cos(yaw) * boostSpeed;

        mc.player.setVelocity(savedVx, mc.player.getVelocity().y, savedVz);
        inBoostJump = true;
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!inBoostJump) return;

        // Stop boosting once the player lands
        if (mc.player.isOnGround()) {
            inBoostJump = false;
            return;
        }

        // Maintain the horizontal speed during the jump arc
        event.setX(savedVx);
        event.setZ(savedVz);
    }
}
