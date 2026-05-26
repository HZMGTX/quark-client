package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * OpenInventory - lets the player open their inventory on the server (sends nothing).
 */
public class OpenInventory extends Module {

    public OpenInventory() {
        super("OpenInventory", "Keeps server-side inventory open while moving", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        // Suppresses close-handled-screen packets in the pipeline.
    }
}
