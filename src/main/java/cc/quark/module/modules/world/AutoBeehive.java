package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class AutoBeehive extends Module {
    private final IntSetting range = register(new IntSetting("Range", "Operation range", 5, 1, 16));
    private final BoolSetting autoMode = register(new BoolSetting("Auto", "Fully automatic", true));
    private int tick = 0;

    public AutoBeehive() { super("AutoBeehive", "Manages beehives and honey collection", Category.WORLD); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || ++tick < 5) return;
        tick = 0;
    }
}
