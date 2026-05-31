package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

public class AutoBroadcast extends Module {

    private final StringSetting message = register(new StringSetting(
            "Message", "Message to broadcast to all players", "Hello from Quark!"));
    private final IntSetting intervalSec = register(new IntSetting(
            "Interval", "Seconds between broadcasts", 60, 5, 600));

    private final TimerUtil timer = new TimerUtil();

    public AutoBroadcast() {
        super("AutoBroadcast", "Sends a configurable message to all players at set intervals", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(intervalSec.get() * 1000L)) return;
        String msg = message.get();
        if (!msg.isEmpty()) {
            mc.player.networkHandler.sendChatMessage(msg);
        }
        timer.reset();
    }
}
