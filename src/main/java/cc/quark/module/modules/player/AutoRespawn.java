package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.client.gui.screen.DeathScreen;

/**
 * AutoRespawn - automatically respawns the player when the death screen appears.
 */
public class AutoRespawn extends Module {

    public AutoRespawn() {
        super("AutoRespawn", "Automatically clicks respawn on the death screen", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Check if the current screen is the death screen
        if (mc.currentScreen instanceof DeathScreen) {
            // Request respawn by calling the player method
            mc.player.requestRespawn();
            mc.setScreen(null);
        }
    }
}
