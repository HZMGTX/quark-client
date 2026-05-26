package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * ScaffoldFast - speeds up bridging by reducing the place delay.
 */
public class ScaffoldFast extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Bridge speed multiplier", 1.5, 1.0, 4.0));

    public ScaffoldFast() {
        super("ScaffoldFast", "Faster scaffold bridging", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Place rate adjusted in scaffold placement logic.
    }
}
