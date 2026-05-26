package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * WaterWalk - keeps the player at the water surface while submerged so they
 * can walk across water rather than sinking.
 */
public class WaterWalk extends Module {

    public WaterWalk() {
        super("WaterWalk", "Walk on top of water", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;
        if (mc.player.isSneaking()) return;
        if (event.getY() < 0) {
            event.setY(0.0);
        }
    }
}
