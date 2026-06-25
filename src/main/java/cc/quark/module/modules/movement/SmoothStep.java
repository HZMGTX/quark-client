package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class SmoothStep extends Module {
    private final DoubleSetting maxHeight = register(new DoubleSetting("MaxHeight", "Max block step height", 1.5, 0.6, 3.0));
    public SmoothStep() { super("SmoothStep", "Smoothly steps up blocks without jumping", Category.MOVEMENT); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.stepHeight = (float) maxHeight.getValue();
    }
    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.stepHeight = 0.6f;
    }
}
