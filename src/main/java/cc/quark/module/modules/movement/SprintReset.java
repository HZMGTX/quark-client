package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * SprintReset - briefly drops sprint on attack to reset knockback W-tapping.
 */
public class SprintReset extends Module {

    public SprintReset() {
        super("SprintReset", "Resets sprint when attacking", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.options.attackKey.isPressed() && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }
    }
}
