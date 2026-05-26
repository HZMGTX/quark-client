package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * NoPushItems - prevents item entities from pushing the player around.
 */
public class NoPushItems extends Module {

    public NoPushItems() {
        super("NoPushItems", "Stops dropped items from pushing you", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Push cancellation enforced in collision mixin.
    }
}
