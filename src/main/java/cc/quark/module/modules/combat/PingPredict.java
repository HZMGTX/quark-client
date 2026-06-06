package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class PingPredict extends Module {
    private final DoubleSetting prediction = register(new DoubleSetting("Prediction", "Position prediction factor", 0.5, 0.0, 2.0));
    private final BoolSetting adaptivePing = register(new BoolSetting("Adaptive", "Adapt to current ping", true));
    public PingPredict() { super("PingPredict", "Predicts enemy position accounting for ping", Category.COMBAT); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
    }
}
