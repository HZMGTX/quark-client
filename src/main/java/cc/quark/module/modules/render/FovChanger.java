package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class FovChanger extends Module {
    private final DoubleSetting fov = register(new DoubleSetting("FOV", "Custom field of view", 90.0, 30.0, 180.0));
    private double originalFov = 70.0;

    public FovChanger() { super("FovChanger", "Overrides your field of view", Category.RENDER); }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        if (mc.options != null) originalFov = mc.options.getFov().getValue();
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        if (mc.options != null) mc.options.getFov().setValue((int)originalFov);
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.options != null) mc.options.getFov().setValue((int)fov.get());
    }
}
