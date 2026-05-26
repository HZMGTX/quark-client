package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * OmniSprint - keeps the player sprinting in every direction, even backwards.
 */
public class OmniSprint extends Module {

    public OmniSprint() {
        super("OmniSprint", "Sprint in all directions", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        boolean moving = mc.player.input.movementForward != 0
                || mc.player.input.movementSideways != 0;
        if (moving && !mc.player.isTouchingWater() && mc.player.getHungerManager().getFoodLevel() > 0) {
            mc.player.setSprinting(true);
        }
    }
}
