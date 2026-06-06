package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class LightningAura extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range", "Range", 4.0, 1.0, 10.0));
    private final BoolSetting smart = register(new BoolSetting("Smart", "Enable smart targeting", true));

    public LightningAura() { super("LightningAura", "Summons lightning via commands on enemies", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
    }
}
