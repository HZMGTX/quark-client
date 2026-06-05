package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class AirBhop extends Module {
    private final DoubleSetting boost = register(new DoubleSetting("Boost", "Horizontal speed boost", 1.3, 1.0, 3.0));
    private boolean wasOnGround = false;

    public AirBhop() { super("AirBhop", "Automatically jumps and boosts when landing", Category.MOVEMENT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        boolean onGround = mc.player.isOnGround();
        if (onGround && !wasOnGround) {
            mc.player.jump();
            var vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * boost.get(), vel.y, vel.z * boost.get());
        }
        wasOnGround = onGround;
    }
}
