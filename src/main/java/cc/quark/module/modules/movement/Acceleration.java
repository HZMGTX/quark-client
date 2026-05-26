package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * Acceleration - scales the player's movement velocity in the move event for a
 * subtle constant speed increase.
 */
public class Acceleration extends Module {

    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Velocity multiplier", 1.15, 1.0, 2.0));

    public Acceleration() {
        super("Acceleration", "Multiplies movement velocity", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mc.player.input.movementForward == 0 && mc.player.input.movementSideways == 0) return;
        event.setX(event.getX() * factor.get());
        event.setZ(event.getZ() * factor.get());
    }
}
