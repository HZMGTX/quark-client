package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeAnnouncer extends Module {

    private final IntSetting intervalMin = register(new IntSetting(
            "Interval", "Minutes between announcements", 30, 1, 120));

    private final ModeSetting format = register(new ModeSetting(
            "Format", "Time display format", "24h", "24h", "12h"));

    private final StringSetting prefix = register(new StringSetting(
            "Prefix", "Message prefix sent before the time", "Current time:"));

    private final BoolSetting toChat = register(new BoolSetting(
            "To Chat", "Broadcast time to server chat (vs local client message only)", false));

    private final BoolSetting localEcho = register(new BoolSetting(
            "Local Echo", "Show time in local chat (not sent to server)", true));

    private static final DateTimeFormatter FMT_24H = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_12H = DateTimeFormatter.ofPattern("h:mm a");

    private final TimerUtil timer = new TimerUtil();

    public TimeAnnouncer() {
        super("TimeAnnouncer", "Announces real-world time in chat at configurable intervals", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(intervalMin.get() * 60_000L)) return;

        DateTimeFormatter fmt = format.is("12h") ? FMT_12H : FMT_24H;
        String time = LocalTime.now().format(fmt);
        String message = prefix.get() + " " + time;

        if (toChat.isEnabled() && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatMessage(message);
        }

        if (localEcho.isEnabled()) {
            mc.player.sendMessage(
                    net.minecraft.text.Text.literal("§7[TimeAnnouncer] §e" + message), false);
        }

        timer.reset();
    }

    @Override
    public String getSuffix() {
        return "every " + intervalMin.get() + "m";
    }
}
