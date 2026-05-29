package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * Hover2 - float in place mid-air; zero Y velocity to cancel gravity;
 * holding Jump adds an upward boost; resets fall distance each tick.
 */
public class Hover2 extends Module {

    private final DoubleSetting upBoost = register(new DoubleSetting(
            "Up Boost", "Upward velocity when space held", 0.08, 0.02, 0.5));
    private final DoubleSetting descend = register(new DoubleSetting(
            "Descend", "Slow descent speed when no key held", 0.0, 0.0, 0.3));

    public Hover2() {
        super("Hover2", "Float in place; hold Jump to rise, Sneak to descend slowly", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;

        double vy;

        if (mc.options.jumpKey.isPressed()) {
            vy = upBoost.get();
        } else if (mc.options.sneakKey.isPressed()) {
            vy = -descend.get() - 0.02;
        } else {
            vy = -descend.get();
        }

        mc.player.setVelocity(mc.player.getVelocity().x, vy, mc.player.getVelocity().z);
        mc.player.fallDistance = 0;
    }
}
