package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class ZoomPlus extends Module {

    private final DoubleSetting fov    = register(new DoubleSetting("FOV",    "Zoom field of view",      15.0, 1.0, 90.0));
    private final BoolSetting   smooth = register(new BoolSetting  ("Smooth", "Smooth zoom transitions", true));

    private double savedFov = 70.0;
    private boolean zooming = false;

    public ZoomPlus() {
        super("ZoomPlus", "Enhanced zoom with configurable FOV and smooth transition", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) savedFov = mc.options.getFov().getValue();
        zooming = true;
    }

    @Override
    public void onDisable() {
        if (mc.options != null && zooming) {
            mc.options.getFov().setValue((int) savedFov);
        }
        zooming = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.options == null) return;
        int target = (int) fov.get();
        int current = mc.options.getFov().getValue();
        if (smooth.isEnabled()) {
            int delta = target - current;
            if (Math.abs(delta) > 1) {
                mc.options.getFov().setValue(current + delta / 4);
            } else {
                mc.options.getFov().setValue(target);
            }
        } else {
            mc.options.getFov().setValue(target);
        }
    }
}
