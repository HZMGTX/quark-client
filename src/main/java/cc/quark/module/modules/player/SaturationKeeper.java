package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * SaturationKeeper - maintains a minimum client-side saturation level.
 */
public class SaturationKeeper extends Module {

    private final DoubleSetting level = register(new DoubleSetting("Level", "Saturation to keep", 5.0, 0.0, 20.0));

    public SaturationKeeper() {
        super("SaturationKeeper", "Keeps saturation topped up", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.getHungerManager().setSaturationLevel((float) level.get());
    }
}
