package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

public class AntiFire extends Module {

    public AntiFire() {
        super("AntiFire", "Extinguishes fire on the player every tick", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnFire()) mc.player.extinguish();
    }
}
