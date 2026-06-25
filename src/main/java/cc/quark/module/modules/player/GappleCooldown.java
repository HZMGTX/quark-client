package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class GappleCooldown extends Module {
    private final BoolSetting smart = register(new BoolSetting("Smart", "Smart mode enabled", true));
    private final DoubleSetting threshold = register(new DoubleSetting("Threshold", "Activation threshold", 8.0, 1.0, 20.0));

    public GappleCooldown() { super("GappleCooldown", "Tracks golden apple cooldown precisely", Category.PLAYER); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
    }
}
