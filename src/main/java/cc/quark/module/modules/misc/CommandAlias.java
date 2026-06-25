package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;

/**
 * CommandAlias - Maps a short alias command to a longer command or chat message.
 *
 * Format for each alias pair: "alias=expansion" where the alias is what you
 * type (starting with '/') and the expansion is what gets sent.
 *
 * Up to 5 alias slots are provided; leave a slot blank to disable it.
 *
 * Example:
 *   Alias 1:  /h
 *   Expand 1: /hub
 *
 * Typing ".h" in chat (with a '.' prefix to avoid conflicts) or "/h" (if the
 * server doesn't recognise it) will send "/hub" instead.
 *
 * The trigger prefix is configurable; default is '.'.
 */
public class CommandAlias extends Module {

    private final StringSetting prefix  = register(new StringSetting(
            "Prefix", "Character that triggers alias lookup (e.g. '.')", "."));

    private final StringSetting alias1  = register(new StringSetting("Alias 1",  "Short alias (e.g. h)",         ""));
    private final StringSetting expand1 = register(new StringSetting("Expand 1", "Expansion (e.g. /hub)",        ""));

    private final StringSetting alias2  = register(new StringSetting("Alias 2",  "Short alias",                  ""));
    private final StringSetting expand2 = register(new StringSetting("Expand 2", "Expansion",                    ""));

    private final StringSetting alias3  = register(new StringSetting("Alias 3",  "Short alias",                  ""));
    private final StringSetting expand3 = register(new StringSetting("Expand 3", "Expansion",                    ""));

    private final StringSetting alias4  = register(new StringSetting("Alias 4",  "Short alias",                  ""));
    private final StringSetting expand4 = register(new StringSetting("Expand 4", "Expansion",                    ""));

    private final StringSetting alias5  = register(new StringSetting("Alias 5",  "Short alias",                  ""));
    private final StringSetting expand5 = register(new StringSetting("Expand 5", "Expansion",                    ""));

    private final String[][] aliases = new String[5][2]; // populated in onTick/on-use

    public CommandAlias() {
        super("CommandAlias", "Creates custom command aliases that map to longer commands", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming()) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;

        String pre = prefix.get();
        if (pre.isEmpty() || !msg.startsWith(pre)) return;

        String input = msg.substring(pre.length()).trim();

        buildAliases();
        for (String[] pair : aliases) {
            String a = pair[0];
            String e = pair[1];
            if (a == null || a.isEmpty() || e == null || e.isEmpty()) continue;

            // Match "alias" or "alias arg1 arg2"
            if (input.equalsIgnoreCase(a) || input.toLowerCase().startsWith(a.toLowerCase() + " ")) {
                String remainder = input.length() > a.length() ? input.substring(a.length()).trim() : "";
                String toSend = e.trim();
                if (!remainder.isEmpty()) toSend = toSend + " " + remainder;

                event.cancel();
                if (toSend.startsWith("/")) {
                    mc.getNetworkHandler().sendCommand(toSend.substring(1));
                } else {
                    mc.getNetworkHandler().sendChatMessage(toSend);
                }
                return;
            }
        }
    }

    private void buildAliases() {
        aliases[0][0] = alias1.get().trim();  aliases[0][1] = expand1.get().trim();
        aliases[1][0] = alias2.get().trim();  aliases[1][1] = expand2.get().trim();
        aliases[2][0] = alias3.get().trim();  aliases[2][1] = expand3.get().trim();
        aliases[3][0] = alias4.get().trim();  aliases[3][1] = expand4.get().trim();
        aliases[4][0] = alias5.get().trim();  aliases[4][1] = expand5.get().trim();
    }
}
