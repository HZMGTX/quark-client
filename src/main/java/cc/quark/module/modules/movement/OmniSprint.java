package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class OmniSprint extends Module {

    private final BoolSetting always = register(new BoolSetting(
            "Always", "Sprint even when standing still", false));

    public OmniSprint() {
        super("OmniSprint", "Sprint in all directions", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;

        boolean moving = mc.player.input.movementForward != 0
                || mc.player.input.movementSideways != 0;

        if (always.isEnabled() || moving) {
            mc.player.setSprinting(true);
        }
    }
}
