package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class ChatMacros extends Module {
    private final BoolSetting gg = register(new BoolSetting("GG", "Type .gg to say 'gg'", true));
    private final BoolSetting lol = register(new BoolSetting("LOL", "Type .lol to say 'lol'", false));
    private final BoolSetting pos = register(new BoolSetting("Pos", "Type .pos to send your coordinates", true));

    public ChatMacros() {
        super("Chat Macros", "Quick shorthand chat commands", Category.MISC, 0);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming() || mc.player == null) return;
        String msg = event.getMessage();
        if (msg == null) return;

        if (gg.isEnabled() && msg.equalsIgnoreCase(".gg")) {
            event.setMessage("gg");
        } else if (lol.isEnabled() && msg.equalsIgnoreCase(".lol")) {
            event.setMessage("lol");
        } else if (pos.isEnabled() && msg.equalsIgnoreCase(".pos")) {
            int x = (int) mc.player.getX();
            int y = (int) mc.player.getY();
            int z = (int) mc.player.getZ();
            event.setMessage("My coords: " + x + " " + y + " " + z);
        }
    }
}
