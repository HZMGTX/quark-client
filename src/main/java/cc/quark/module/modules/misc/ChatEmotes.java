package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;

import java.util.Map;

public class ChatEmotes extends Module {

    private static final Map<String, String> EMOTES = Map.of(
        ":smile:", "😊",
        ":heart:", "❤️",
        ":sword:", "⚔️",
        ":fire:", "🔥",
        ":skull:", "💀",
        ":gem:", "💎",
        ":star:", "⭐",
        ":100:", "💯",
        ":wave:", "👋",
        ":thumbsup:", "👍"
    );

    public ChatEmotes() {
        super("Chat Emotes", "Converts :emote: shortcuts to Unicode emojis", Category.MISC, 0);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming() || mc.player == null) return;
        String msg = event.getMessage();
        if (msg == null) return;
        for (Map.Entry<String, String> entry : EMOTES.entrySet()) {
            msg = msg.replace(entry.getKey(), entry.getValue());
        }
        event.setMessage(msg);
    }
}
