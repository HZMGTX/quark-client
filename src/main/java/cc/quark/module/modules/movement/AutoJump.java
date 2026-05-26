package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * AutoJump - automatically jumps whenever the player is moving on the ground.
 */
public class AutoJump extends Module {

    public AutoJump() {
        super("AutoJump", "Jumps automatically while moving", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0) {
            mc.player.jump();
        }
    }
}
