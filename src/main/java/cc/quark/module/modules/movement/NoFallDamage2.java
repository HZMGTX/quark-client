package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;

public class NoFallDamage2 extends Module {

    public NoFallDamage2() {
        super("NoFallDamage2", "Spoof onGround when falling fast to prevent fall damage", Category.MOVEMENT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (mc.player.getVelocity().y < -2.0) {
            event.setOnGround(true);
            mc.player.fallDistance = 0;
        }
    }
}
