package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

import java.util.LinkedHashMap;
import java.util.Map;

public class ChatBot extends Module {

    private final BoolSetting respondToHi  = register(new BoolSetting("Respond to Hi",   "Reply when someone says hi",    true));
    private final BoolSetting respondToGG  = register(new BoolSetting("Respond to GG",   "Reply when someone says GG",    true));
    private final BoolSetting respondToQ   = register(new BoolSetting("Respond to ?",    "Reply when asked about client", true));

    private final TimerUtil cooldown = new TimerUtil();

    // Trigger → Response pairs
    private final Map<String, String> triggers = new LinkedHashMap<>();

    public ChatBot() {
        super("ChatBot", "Pattern-matches incoming chat and auto-replies to configured triggers", Category.PLAYER);
        triggers.put("hi",      "Hey!");
        triggers.put("hello",   "Hello there!");
        triggers.put("gg",      "GG!");
        triggers.put("good game", "GG wp!");
        triggers.put("what client", "Quark client!");
        triggers.put("what hack",   "Quark client!");
    }

    @Override
    public void onEnable() {
        cooldown.reset();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;
        if (!cooldown.hasReached(2000)) return;

        String raw = event.getMessage();
        // Don't reply to our own messages
        String myName = mc.player.getName().getString();
        if (raw.startsWith(myName + ":") || raw.startsWith("<" + myName + ">")) return;

        String lower = raw.toLowerCase();

        for (Map.Entry<String, String> entry : triggers.entrySet()) {
            String trigger = entry.getKey();
            if (!lower.contains(trigger)) continue;

            // Check per-trigger BoolSettings
            if ((trigger.equals("hi") || trigger.equals("hello")) && !respondToHi.isEnabled()) continue;
            if ((trigger.equals("gg") || trigger.equals("good game")) && !respondToGG.isEnabled()) continue;
            if ((trigger.contains("client") || trigger.contains("hack")) && !respondToQ.isEnabled()) continue;

            ChatUtil.send(entry.getValue());
            cooldown.reset();
            return;
        }
    }
}
