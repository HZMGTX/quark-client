package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

public class FOVChanger extends Module {

    private final IntSetting fov = register(new IntSetting(
            "FOV", "Field of view in degrees", 90, 30, 160));

    private int savedFov = 70;

    public FOVChanger() {
        super("FOVChanger", "Overrides the game's field of view independently of vanilla FOV settings", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) savedFov = mc.options.getFov().getValue();
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.getFov().setValue(savedFov);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.options == null) return;
        mc.options.getFov().setValue(fov.get());
    }
}
