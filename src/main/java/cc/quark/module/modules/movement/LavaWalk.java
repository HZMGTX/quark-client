package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * LavaWalk - keeps the player floating at the lava surface so they do not sink.
 */
public class LavaWalk extends Module {

    public LavaWalk() {
        super("LavaWalk", "Float on top of lava", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!mc.player.isInLava()) return;
        if (mc.player.isSneaking()) return;
        if (event.getY() < 0) {
            event.setY(0.0);
        }
    }
}
