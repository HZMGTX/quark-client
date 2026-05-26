package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * AutoSprintToggle - automatically sprints whenever walking forward.
 */
public class AutoSprintToggle extends Module {

    public AutoSprintToggle() {
        super("AutoSprintToggle", "Always sprint forward", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.input.movementForward > 0
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
