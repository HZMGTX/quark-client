package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

public class AutoStatus extends Module {

    private final IntSetting intervalSec = register(new IntSetting(
            "IntervalSec", "Seconds between status messages", 120, 30, 600));
    private final ModeSetting message = register(new ModeSetting(
            "Message", "Preset message to broadcast", "Hello", "Hello", "AFK", "Custom"));
    private final StringSetting customText = register(new StringSetting(
            "CustomText", "Text to broadcast when Message is set to Custom", "Quark.cc user"));

    private final TimerUtil timer = new TimerUtil();

    public AutoStatus() {
        super("AutoStatus", "Periodically broadcasts a preset status message to chat", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(intervalSec.get() * 1000L)) return;

        String msg = switch (message.get()) {
            case "AFK" -> "I am AFK";
            case "Custom" -> customText.get();
            default -> "Hello from Quark!";
        };

        mc.player.networkHandler.sendChatMessage(msg);
        timer.reset();
    }
}
