package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * ChatFilter - toggle that filters spam/advertising from incoming chat.
 */
public class ChatFilter extends Module {

    private final BoolSetting ads = register(new BoolSetting("Ads", "Filter advertisement messages", true));
    private final BoolSetting caps = register(new BoolSetting("Caps", "Filter all-caps messages", false));

    public ChatFilter() {
        super("ChatFilter", "Filters unwanted chat messages", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Filtering enforced by chat mixin; settings exposed here.
    }
}
