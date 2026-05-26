package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * BridgeAssist - sneaks automatically near edges to help with bridging.
 */
public class BridgeAssist extends Module {

    private final BoolSetting autoSneak = register(new BoolSetting("AutoSneak", "Sneak at block edges", true));

    public BridgeAssist() {
        super("BridgeAssist", "Helps prevent falling while bridging", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (autoSneak.isEnabled() && !mc.player.isOnGround()) {
            mc.options.sneakKey.setPressed(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) mc.options.sneakKey.setPressed(false);
    }
}
