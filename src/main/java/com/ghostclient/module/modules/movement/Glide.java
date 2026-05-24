package com.ghostclient.module.modules.movement;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventMove;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.DoubleSetting;

/**
 * Glide - replaces the normal free-fall acceleration with a slow, controlled descent,
 * allowing the player to glide horizontally after jumping or falling off a ledge.
 *
 * <p>The Y component in the move event is clamped so the player sinks at most
 * {@link #fallSpeed} blocks per tick, producing a gentle floating effect.
 */
public class Glide extends Module {

    private final DoubleSetting fallSpeed = register(new DoubleSetting(
            "Fall Speed", "Maximum downward speed while gliding (blocks/tick)", 0.05, 0.0, 0.5));

    public Glide() {
        super("Glide", "Slows the player's descent for a gliding effect", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        // Only glide while in the air
        if (mc.player.isOnGround()) return;
        // Don't interfere with upward movement
        if (event.getY() >= 0) return;

        // Clamp descent to the configured fall speed
        event.setY(-fallSpeed.get());
        mc.player.fallDistance = 0;
    }
}
