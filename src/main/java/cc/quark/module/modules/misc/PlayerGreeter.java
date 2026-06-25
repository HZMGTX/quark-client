package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

public class PlayerGreeter extends Module {
    private final BoolSetting enabled2 = register(new BoolSetting("Active", "Feature active", true));
    private final StringSetting config = register(new StringSetting("Config", "Configuration string", "default"));

    public PlayerGreeter() { super("PlayerGreeter", "Auto-greets players when they join", Category.MISC); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
    }
}
