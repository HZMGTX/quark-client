package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

public class Sneak extends Module {

    public Sneak() {
        super("Sneak", "Permanently auto-sneaks the player", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.setSneaking(true);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setSneaking(false);
    }
}
