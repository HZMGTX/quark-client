package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * NoSlow - keeps sprinting while using items so movement is not reduced.
 */
public class NoSlow extends Module {

    private final BoolSetting keepSprint = register(new BoolSetting(
            "KeepSprint", "Force sprint while using items", true));

    public NoSlow() {
        super("NoSlow", "Removes item-use slowdown", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isUsingItem()) return;
        if (keepSprint.isEnabled() && mc.player.input.movementForward > 0) {
            mc.player.setSprinting(true);
        }
    }
}
