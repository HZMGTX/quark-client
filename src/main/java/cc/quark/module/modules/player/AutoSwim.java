package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

public class AutoSwim extends Module {

    private final IntSetting oxygenThreshold = register(new IntSetting(
            "OxygenThreshold", "Air supply level below which to start surfacing", 100, 0, 300));

    public AutoSwim() {
        super("AutoSwim", "Automatically surfaces when drowning and swims to shore", Category.PLAYER);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (!mc.player.isSubmergedInWater()) return;

        if (mc.player.getAir() <= oxygenThreshold.get()) {
            mc.player.jumping = true;
            mc.options.forwardKey.setPressed(true);
        } else {
            mc.options.forwardKey.setPressed(false);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.forwardKey.setPressed(false);
        }
    }
}
