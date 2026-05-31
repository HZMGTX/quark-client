package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class NoSwingDelay extends Module {

    private final BoolSetting enabled = register(new BoolSetting("Enabled", "Remove minimum swing interval", true));

    public NoSwingDelay() {
        super("NoSwingDelay", "Removes the minimum swing interval allowing attacks at any timing", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!enabled.isEnabled()) return;
        // Force attack cooldown to appear full every tick
        mc.player.resetLastAttackedTicks();
    }
}
