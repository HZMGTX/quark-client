package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class StepUp extends Module {

    private final DoubleSetting stepHeight = register(new DoubleSetting(
            "StepHeight", "Maximum step height in blocks", 1.0, 0.6, 2.5));

    public StepUp() {
        super("StepUp", "Steps up blocks higher than vanilla (up to 2.5 blocks)", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) mc.player.stepHeight = (float) stepHeight.get();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.stepHeight = 0.6f;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.stepHeight = (float) stepHeight.get();
    }
}
