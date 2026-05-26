package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * NoJumpDelay - removes the small cooldown between jumps so the player can
 * jump again immediately upon landing while holding the jump key.
 */
public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", "Removes jump cooldown", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.options.jumpKey.isPressed()) return;
        if (mc.player.isOnGround()) {
            mc.player.jump();
        }
    }
}
