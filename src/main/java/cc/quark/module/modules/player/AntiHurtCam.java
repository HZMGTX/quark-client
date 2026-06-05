package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

public class AntiHurtCam extends Module {
    public AntiHurtCam() { super("AntiHurtCam", "Disables camera shake when taking damage", Category.PLAYER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        mc.player.hurtTime = 0;
    }
}
