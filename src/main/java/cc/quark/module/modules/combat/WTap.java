package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class WTap extends Module {

    private final BoolSetting reSprint = register(new BoolSetting(
            "Re-Sprint", "Automatically sprint again after the reset", true));

    private int resetTicks = 0;

    public WTap() {
        super("WTap", "Sprint-resets on every attack to maximise knockback", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        mc.player.setSprinting(false);
        resetTicks = 2;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || !reSprint.isEnabled()) return;
        if (resetTicks > 0 && --resetTicks == 0) {
            mc.player.setSprinting(true);
        }
    }
}
