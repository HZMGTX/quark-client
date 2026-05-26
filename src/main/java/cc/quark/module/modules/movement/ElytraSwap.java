package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * ElytraSwap - applies a small forward boost while gliding with elytra.
 */
public class ElytraSwap extends Module {

    private final BoolSetting onlyFlying = register(new BoolSetting(
            "OnlyFlying", "Only boost while fall-flying", true));

    public ElytraSwap() {
        super("ElytraSwap", "Boosts elytra gliding", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (onlyFlying.isEnabled() && !mc.player.isFallFlying()) return;
        if (!mc.player.isFallFlying()) return;
        mc.player.setVelocity(mc.player.getVelocity().multiply(1.05, 1.0, 1.05));
    }
}
