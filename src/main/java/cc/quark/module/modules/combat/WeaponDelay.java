package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class WeaponDelay extends Module {

    private final BoolSetting enabled = register(new BoolSetting("Enabled", "Spoof attack cooldown to always show max", true));

    public WeaponDelay() {
        super("WeaponDelay", "Spoof attack cooldown to always show max", Category.COMBAT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (!enabled.isEnabled()) return;
        if (mc.player == null) return;

        // Reset the attack cooldown so it always reads as fully charged
        mc.player.resetLastAttackedTicks();
    }
}
