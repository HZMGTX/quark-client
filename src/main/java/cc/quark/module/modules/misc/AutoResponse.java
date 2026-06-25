package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

public class AutoResponse extends Module {

    private final StringSetting response    = register(new StringSetting("Response",   "Message to auto-reply with",          "I am AFK right now!"));
    private final IntSetting    cooldownSec = register(new IntSetting("Cooldown",      "Seconds between auto-replies",        30, 1, 300));
    private final BoolSetting   whisperOnly = register(new BoolSetting("WhisperOnly",  "Only respond to /msg or /whisper",    true));
    private final BoolSetting   notifySelf  = register(new BoolSetting("NotifySelf",   "Notify in chat when a reply is sent", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoResponse() {
        super("AutoResponse", "Automatically responds to incoming private messages with a configured reply", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;

        String msg = event.getMessage();
        if (msg == null) return;

        boolean isWhisper = msg.contains("whispers to you") || msg.contains("-> You") || msg.contains("msg");
        if (whisperOnly.isEnabled() && !isWhisper) return;

        if (!timer.hasReached(cooldownSec.get() * 1000L)) return;
        timer.reset();

        String reply = response.get();
        if (reply.isEmpty()) return;

        mc.execute(() -> {
            if (mc.player == null) return;
            mc.player.networkHandler.sendChatMessage(reply);
            if (notifySelf.isEnabled()) {
                ChatUtil.info("[AutoResponse] Sent: " + reply);
            }
        });
    }
}
