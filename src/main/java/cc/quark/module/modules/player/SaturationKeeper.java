package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class SaturationKeeper extends Module {

    private final DoubleSetting level = register(new DoubleSetting("Level", "Saturation level to maintain", 5.0, 0.0, 20.0));
    private float savedSaturation = -1f;

    public SaturationKeeper() {
        super("SaturationKeeper", "Keeps saturation at a set level", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        var hungerManager = mc.player.getHungerManager();
        if (savedSaturation < 0) {
            savedSaturation = hungerManager.getSaturationLevel();
        }
        hungerManager.setSaturationLevel((float) level.get());
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (savedSaturation >= 0) {
            mc.player.getHungerManager().setSaturationLevel(savedSaturation);
            savedSaturation = -1f;
        }
    }
}
