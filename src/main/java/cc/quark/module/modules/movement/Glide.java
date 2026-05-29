package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;

/**
 * Glide - reduces gravity while airborne so the player descends slowly.
 *
 * <ul>
 *   <li><b>Slow</b>   - clamp downward Y to a gentle terminal velocity.</li>
 *   <li><b>Float</b>  - zero out all downward Y velocity (hover).</li>
 *   <li><b>Boost</b>  - apply a small upward force each tick to sustain height.</li>
 * </ul>
 */
public class Glide extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Glide behaviour", "Slow", "Slow", "Float", "Boost"));
    private final DoubleSetting fallSpeed = register(new DoubleSetting(
            "Fall Speed", "Max downward speed in Slow mode (blocks/tick)", 0.05, 0.0, 0.5));
    private final DoubleSetting boostForce = register(new DoubleSetting(
            "Boost Force", "Upward force applied each tick in Boost mode", 0.04, 0.0, 0.2));

    public Glide() {
        super("Glide", "Reduce gravity for a slow, gliding descent", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        switch (mode.get()) {
            case "Slow" -> {
                // Only clamp when falling (Y negative)
                if (event.getY() < -fallSpeed.get()) {
                    event.setY(-fallSpeed.get());
                    mc.player.fallDistance = 0;
                }
            }
            case "Float" -> {
                if (event.getY() < 0) {
                    event.setY(0.0);
                    mc.player.fallDistance = 0;
                }
            }
            case "Boost" -> {
                // Apply upward force — net effect opposes gravity
                event.setY(event.getY() + boostForce.get());
                mc.player.fallDistance = 0;
            }
        }
    }
}
