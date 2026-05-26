package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * InstantStop - zeroes out horizontal movement via the move event when sneaking.
 */
public class InstantStop extends Module {

    public InstantStop() {
        super("InstantStop", "Cancel movement while sneaking", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;
        event.setX(0.0);
        event.setZ(0.0);
    }
}
