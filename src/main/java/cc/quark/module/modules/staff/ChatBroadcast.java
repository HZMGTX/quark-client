package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class ChatBroadcast extends Module {

    private final StringSetting message = register(new StringSetting(
            "Message", "Announcement text to broadcast", "Server announcement!"));
    private final IntSetting intervalSeconds = register(new IntSetting(
            "Interval", "Seconds between each broadcast", 30, 5, 300));
    private final ModeSetting method = register(new ModeSetting(
            "Method", "Chat command used to broadcast", "say", "say", "broadcast", "announce", "alert"));
    private final BoolSetting addPrefix = register(new BoolSetting(
            "Prefix", "Prepend [BROADCAST] to the message", true));

    private int tickTimer = 0;

    public ChatBroadcast() {
        super("ChatBroadcast", "Broadcasts timed announcements to all players", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        tickTimer = 0;
        mc.getEventBus().subscribe(this);
        sendBroadcast(); // send immediately on enable
        ChatUtil.info("§6[ChatBroadcast] §fBroadcasting every §e" + intervalSeconds.get() + "s§f.");
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        tickTimer = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) { disable(); return; }
        tickTimer++;
        if (tickTimer >= intervalSeconds.get() * 20) {
            tickTimer = 0;
            sendBroadcast();
        }
    }

    private void sendBroadcast() {
        if (mc.player == null) return;
        String msg = message.get();
        if (addPrefix.isEnabled()) msg = "[BROADCAST] " + msg;
        mc.player.networkHandler.sendChatCommand(method.get() + " " + msg);
        ChatUtil.info("§6[ChatBroadcast] §fSent: §7" + msg);
    }
}
