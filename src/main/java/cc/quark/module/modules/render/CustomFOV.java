package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

public class CustomFov extends Module {

    private final IntSetting fov = register(new IntSetting("FOV", "Field of view value", 90, 30, 150));
    private double saved = 70;

    public CustomFov() {
        super("CustomFov", "Forces a custom field of view value", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) saved = mc.options.getFov().getValue();
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.getFov().setValue((int) saved);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.options == null) return;
        mc.options.getFov().setValue(fov.get());
    }
}
