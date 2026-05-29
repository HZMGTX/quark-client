package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * InstantStop - on Sneak key down-transition, instantly zero all velocity.
 * Detects the key press edge (was not pressed → now pressed) so it fires once
 * per press rather than every tick while sneaking.
 */
public class InstantStop extends Module {

    private boolean wasSneaking = false;

    public InstantStop() {
        super("InstantStop", "Instantly zero all velocity on Sneak press", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasSneaking = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean sneaking = mc.player.isSneaking();

        // Detect down-transition: key wasn't pressed last tick but is now
        if (sneaking && !wasSneaking) {
            mc.player.setVelocity(0.0, 0.0, 0.0);
        }

        wasSneaking = sneaking;
    }
}
