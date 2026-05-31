package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

public class ChatRepeater extends Module {

    private final IntSetting intervalSeconds = register(new IntSetting(
            "Interval", "Seconds between repeated messages", 30, 5, 300));

    private final BoolSetting onlySelf = register(new BoolSetting(
            "OnlySelf", "Only repeat messages you sent yourself", true));

    private final BoolSetting notifyRepeat = register(new BoolSetting(
            "Notify", "Show a local notification before repeating", true));

    private final TimerUtil timer = new TimerUtil();
    private String lastMessage = null;

    public ChatRepeater() {
        super("ChatRepeater", "Repeatedly sends the last chat message at a set interval", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
        lastMessage = null;
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (mc.player == null) return;
        // Only capture outgoing (sent) messages
        if (event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg != null && !msg.isEmpty() && !msg.startsWith("/")) {
            lastMessage = msg;
            timer.reset();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (lastMessage == null || lastMessage.isEmpty()) return;
        if (!timer.hasReached(intervalSeconds.get() * 1000L)) return;

        if (notifyRepeat.isEnabled()) {
            ChatUtil.info("ChatRepeater: Resending '" + lastMessage + "'");
        }

        mc.player.networkHandler.sendChatMessage(lastMessage);
        timer.reset();
    }

    @Override
    public String getSuffix() {
        return lastMessage != null ? lastMessage.substring(0, Math.min(lastMessage.length(), 12)) : "none";
    }
}
