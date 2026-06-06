package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class StepPlus extends Module {
    private final DoubleSetting height = register(new DoubleSetting("Height", "Step height", 2.0, 1.0, 5.0));

    public StepPlus() { super("StepPlus", "Step up blocks of any height instantly", Category.MOVEMENT); }
    @Override public void onEnable() { if (mc.player != null) mc.player.stepHeight = (float) height.get(); }
    @Override public void onDisable() { if (mc.player != null) mc.player.stepHeight = 0.6f; }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        mc.player.stepHeight = (float) height.get();
    }
}
