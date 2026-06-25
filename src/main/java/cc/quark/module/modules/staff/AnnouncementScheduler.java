package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class AnnouncementScheduler extends Module {

    private final StringSetting message1 = register(new StringSetting(
            "Message 1", "First announcement message (leave blank to skip)", "Welcome to the server!"));
    private final StringSetting message2 = register(new StringSetting(
            "Message 2", "Second announcement message (leave blank to skip)", "Please follow the rules."));
    private final StringSetting message3 = register(new StringSetting(
            "Message 3", "Third announcement message (leave blank to skip)", ""));
    private final IntSetting interval = register(new IntSetting(
            "Interval (Seconds)", "Seconds between each announcement cycle", 120, 60, 600));
    private final BoolSetting broadcastSay = register(new BoolSetting(
            "Broadcast Say", "Use /say instead of /broadcast for maximum visibility", true));

    private int tickCounter = 0;
    private int messageIndex = 0;
    private String[] messages;

    public AnnouncementScheduler() {
        super("AnnouncementScheduler", "Sends periodic server announcements on a configurable interval", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        tickCounter = 0;
        messageIndex = 0;
        messages = buildMessages();
        if (messages.length == 0) {
            ChatUtil.warn("[AnnounceSched] No messages configured.");
            disable();
            return;
        }
        ChatUtil.info("§6[AnnounceSched] §fScheduled §e" + messages.length
                + " §fmessage(s) every §e" + interval.get() + "s.");
        sendNext(); // send first message immediately
    }

    @Override
    public void onDisable() {
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        tickCounter++;
        if (tickCounter >= interval.get() * 20) {
            tickCounter = 0;
            sendNext();
        }
    }

    private void sendNext() {
        if (mc.player == null || messages == null || messages.length == 0) return;
        String msg = messages[messageIndex % messages.length];
        messageIndex++;
        String cmd = broadcastSay.isEnabled() ? "say " + msg : "broadcast " + msg;
        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.info("§6[AnnounceSched] §fSent: §7" + msg);
    }

    private String[] buildMessages() {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (!message1.get().trim().isEmpty()) list.add(message1.get().trim());
        if (!message2.get().trim().isEmpty()) list.add(message2.get().trim());
        if (!message3.get().trim().isEmpty()) list.add(message3.get().trim());
        return list.toArray(new String[0]);
    }
}
