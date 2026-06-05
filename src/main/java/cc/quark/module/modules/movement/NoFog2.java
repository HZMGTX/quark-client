package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

public class NoFog2 extends Module {
    public NoFog2() { super("NoFog2","Disables all fog in the game",Category.MOVEMENT); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null) return;
        // Fog removal handled via FogCallback mixin; this module exposes the toggle.
        // cloud render
    }
}
