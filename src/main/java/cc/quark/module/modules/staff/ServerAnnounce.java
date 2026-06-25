package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class ServerAnnounce extends Module {

    private final StringSetting prefix = register(new StringSetting(
            "Prefix", "Message prefix shown before the announcement", "[Staff]"));
    private final StringSetting message = register(new StringSetting(
            "Message", "Announcement text to broadcast", "Server maintenance soon"));
    private final StringSetting color = register(new StringSetting(
            "Color Code", "Minecraft color code char (0-9, a-f) for prefix", "c"));
    private final BoolSetting repeat = register(new BoolSetting(
            "Repeat", "Repeat the announcement on an interval", false));
    private final IntSetting intervalTicks = register(new IntSetting(
            "Interval Ticks", "Ticks between repeated announcements (20 = 1 second)", 400, 20, 6000));
    private final BoolSetting useTitle = register(new BoolSetting(
            "Use /say", "Use /say command (broadcasts to all players)", true));

    private int ticksSinceLast = 0;
    private boolean sentOnce = false;

    public ServerAnnounce() {
        super("ServerAnnounce", "Broadcasts announcements with configurable prefix/color", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        ticksSinceLast = intervalTicks.get(); // send immediately on first tick
        sentOnce = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        ticksSinceLast++;
        if (ticksSinceLast < intervalTicks.get()) return;
        ticksSinceLast = 0;

        sendAnnouncement();

        if (!repeat.isEnabled()) {
            if (sentOnce) { disable(); return; }
            sentOnce = true;
        }
    }

    private void sendAnnouncement() {
        String col = color.get().trim();
        if (col.isEmpty()) col = "f";
        String formatted = "§" + col + prefix.get() + " §f" + message.get();

        if (useTitle.isEnabled()) {
            // /say echoes to all players
            mc.player.networkHandler.sendChatCommand("say " + formatted);
        } else {
            mc.player.networkHandler.sendChatMessage(formatted);
        }
        ChatUtil.info("§6[ServerAnnounce] §fBroadcasted: " + formatted);
    }
}
