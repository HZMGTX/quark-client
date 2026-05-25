package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class AutoSprint extends Module {

    private final BoolSetting omniSprint = register(new BoolSetting(
            "Omni Sprint", "Sprint in all directions, not just forward", true));

    public AutoSprint() {
        super("AutoSprint", "Automatically sprints while moving", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setSprinting(false);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;

        boolean moving = omniSprint.isEnabled()
                ? mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0
                : mc.player.input.movementForward > 0;

        if (moving) mc.player.setSprinting(true);
    }
}
