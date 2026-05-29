package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * NoGravity - simpler than GravityControl: zero Y velocity each tick while
 * airborne so the player simply floats at their current height.
 * Hold Jump to rise, Sneak to descend slowly.
 */
public class NoGravity extends Module {

    public NoGravity() {
        super("NoGravity", "Zero Y velocity while airborne; Jump to rise, Sneak to sink", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;

        double vy;

        if (mc.options.jumpKey.isPressed()) {
            vy = 0.15;
        } else if (mc.options.sneakKey.isPressed()) {
            vy = -0.08;
        } else {
            vy = 0.0;
        }

        mc.player.setVelocity(mc.player.getVelocity().x, vy, mc.player.getVelocity().z);
        mc.player.fallDistance = 0;
    }
}
