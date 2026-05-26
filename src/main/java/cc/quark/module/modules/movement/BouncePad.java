package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class BouncePad extends Module {

    private final DoubleSetting boost = register(new DoubleSetting("Boost", "Upward velocity on landing", 0.8, 0.1, 3.0));
    private boolean wasFalling = false;

    public BouncePad() {
        super("BouncePad", "Launches you upward when landing on the ground", Category.MOVEMENT);
    }

    @Override
    public void onEnable() { wasFalling = false; }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean falling = mc.player.getVelocity().y < -0.1;
        if (wasFalling && mc.player.isOnGround()) {
            mc.player.addVelocity(0, boost.get(), 0);
        }
        wasFalling = falling;
    }
}
