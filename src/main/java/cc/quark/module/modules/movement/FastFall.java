package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class FastFall extends Module {

    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Fall speed multiplier", 3.0, 1.0, 10.0));

    public FastFall() {
        super("FastFall", "Fall faster when sneaking", Category.MOVEMENT);
    }

    

    

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround() && mc.options.sneakKey.isPressed()) {
            mc.player.setVelocity(mc.player.getVelocity().x, -speed.get() * 0.1, mc.player.getVelocity().z);
        }
    }
}
