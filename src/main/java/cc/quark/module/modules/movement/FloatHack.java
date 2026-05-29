package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * FloatHack - locks the player's Y position while airborne by zeroing the
 * vertical component of each move event.  The player hovers at whatever height
 * they were at when they became airborne.
 *
 * <p>Jump and sneak keys can optionally still adjust vertical position at a
 * controlled speed so the player can fly up/down manually.
 */
public class FloatHack extends Module {

    private final BoolSetting manualControl = register(new BoolSetting(
            "Manual Y Control", "Use jump/sneak to move up/down while floating", true));
    private final DoubleSetting vertSpeed = register(new DoubleSetting(
            "Vert Speed", "Speed of manual vertical movement (blocks/tick)", 0.1, 0.01, 1.0));

    public FloatHack() {
        super("FloatHack", "Lock Y position in air — hover at current height", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        if (manualControl.isEnabled()) {
            boolean jumpHeld  = mc.options.jumpKey.isPressed();
            boolean sneakHeld = mc.options.sneakKey.isPressed();

            if (jumpHeld) {
                event.setY(vertSpeed.get());
            } else if (sneakHeld) {
                event.setY(-vertSpeed.get());
            } else {
                event.setY(0.0);
            }
        } else {
            event.setY(0.0);
        }

        mc.player.fallDistance = 0;
    }
}
