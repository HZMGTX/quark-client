package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

public class AutoReply2 extends Module {

    private final StringSetting trigger = register(new StringSetting(
            "Trigger", "Word that triggers an auto-reply when seen in chat", "!ping"));
    private final StringSetting reply = register(new StringSetting(
            "Reply", "Message sent in response to the trigger", "Pong!"));

    private final TimerUtil cooldown = new TimerUtil();

    public AutoReply2() {
        super("AutoReply2", "Replies to chat mentions containing a configurable trigger word", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming() || mc.player == null) return;
        String msg = event.getMessage();
        String trig = trigger.get();
        if (trig.isEmpty()) return;
        if (msg.toLowerCase().contains(trig.toLowerCase())) {
            if (!cooldown.hasReached(3000)) return;
            String replyText = reply.get();
            if (!replyText.isEmpty()) {
                mc.player.networkHandler.sendChatMessage(replyText);
            }
            cooldown.reset();
        }
    }
}
