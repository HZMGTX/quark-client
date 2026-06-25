package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

public class CustomPrefix extends Module {

    private final ModeSetting prefix = register(new ModeSetting(
            "Prefix", "Prefix to prepend to outgoing messages",
            "[VIP]",
            "[VIP]", "[ADMIN]", "[MOD]", "[MVP]", "[PRO]", "[Quark]", "Custom"));

    private final BoolSetting cancelEmpty = register(new BoolSetting(
            "CancelEmpty", "Cancel sending if message becomes empty after prefix stripping", true));

    private final BoolSetting onlyOutgoing = register(new BoolSetting(
            "OnlyOutgoing", "Only modify outgoing (sent) messages, not received ones", true));

    public CustomPrefix() {
        super("CustomPrefix", "Prepends a custom prefix to all outgoing chat messages", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (mc.player == null) return;
        if (onlyOutgoing.isEnabled() && event.isIncoming()) return;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;

        // Don't prefix commands
        if (msg.startsWith("/")) return;

        String chosenPrefix = prefix.get();
        if (chosenPrefix.equals("Custom")) {
            // "Custom" mode: use a hardcoded placeholder (user would change this)
            chosenPrefix = "[Quark.cc]";
        }

        String modified = chosenPrefix + " " + msg;
        event.setMessage(modified);
    }
}
