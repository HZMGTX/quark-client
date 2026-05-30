package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * AntiSwim - prevents the automatic swim animation by setting swimming to
 * false each tick. AllowManual setting allows sneak-triggered swimming.
 */
public class AntiSwim extends Module {

    private final BoolSetting allowManual = register(new BoolSetting(
            "AllowManual", "Still allow swimming when sneak key is held", true));

    public AntiSwim() {
        super("AntiSwim", "Prevents automatic swim animation in water", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        // Nothing to reset; vanilla will re-enable swimming naturally
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSwimming()) return;

        // If AllowManual is on, permit swimming while sneak is held
        if (allowManual.isEnabled() && mc.player.isSneaking()) return;

        mc.player.setSwimming(false);
    }
}
