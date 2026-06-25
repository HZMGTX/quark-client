package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

public class DiscordRPC extends Module {

    private final BoolSetting   showServer  = register(new BoolSetting  ("Show Server",  "Show current server in presence", true));
    private final BoolSetting   showHealth  = register(new BoolSetting  ("Show Health",  "Show health in details",          false));
    private final StringSetting status      = register(new StringSetting("Status",       "Custom status text",              "Playing Quark.cc"));

    public DiscordRPC() {
        super("DiscordRPC", "Shows Minecraft game status in Discord Rich Presence", Category.MISC);
    }

    @EventHandler
    public void onTick(EventTick event) {
        // Discord RPC integration would require native library
        // This module acts as a placeholder/toggle for external integration
        if (mc.player == null) return;
    }
}
