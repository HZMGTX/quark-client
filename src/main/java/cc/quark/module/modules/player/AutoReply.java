package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * AutoReply - automatically replies to incoming whispers.
 */
public class AutoReply extends Module {

    private final BoolSetting afk = register(new BoolSetting("AFK", "Reply with an AFK notice", true));

    public AutoReply() {
        super("AutoReply", "Auto-replies to direct messages", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Reply dispatch handled via chat receive mixin.
    }
}
