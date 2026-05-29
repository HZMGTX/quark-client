package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * OmniSprint - force sprint in all directions every tick regardless of movement
 * direction (vanilla only sprints when moving forward).
 */
public class OmniSprint extends Module {

    private final BoolSetting always = register(new BoolSetting(
            "Always", "Sprint even when standing still (no input)", false));

    public OmniSprint() {
        super("OmniSprint", "Sprint in all directions including sideways and backward", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setSprinting(false);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;
        if (mc.player.isUsingItem()) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;

        if (always.isEnabled() || moving) {
            mc.player.setSprinting(true);
        }
    }
}
