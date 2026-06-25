package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class CrouchSlide extends Module {
    private final DoubleSetting speed = register(new DoubleSetting("Speed", "Movement speed", 1.0, 0.1, 10.0));
    private final BoolSetting smart = register(new BoolSetting("Smart", "Smart mode", true));

    public CrouchSlide() { super("CrouchSlide", "Slide at high speed while crouching", Category.MOVEMENT); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
    }
}
