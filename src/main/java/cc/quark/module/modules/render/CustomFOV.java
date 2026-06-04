package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class CustomFOV extends Module {

    private final DoubleSetting fov = register(new DoubleSetting(
            "FOV", "Custom field of view value", 70.0, 30.0, 120.0));

    private final BoolSetting lock = register(new BoolSetting(
            "Lock", "Lock FOV regardless of speed or status effects", true));

    public CustomFOV() {
        super("CustomFOV", "Sets custom FOV independent of speed", Category.RENDER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.options == null) return;

        // Override the game's FOV option each tick
        if (lock.isEnabled()) {
            mc.options.getFov().setValue((int) fov.get());
        }
    }
}
