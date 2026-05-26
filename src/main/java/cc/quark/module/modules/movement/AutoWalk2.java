package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * AutoWalk2 - automatically holds the forward key.
 */
public class AutoWalk2 extends Module {

    public AutoWalk2() {
        super("AutoWalk2", "Walks forward automatically", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.input.movementForward = 1.0f;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.input.movementForward = 0.0f;
        }
    }
}
