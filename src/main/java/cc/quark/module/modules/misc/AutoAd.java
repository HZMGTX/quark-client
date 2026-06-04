package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

public class AutoAd extends Module {
    private final StringSetting message = register(new StringSetting("Message","Advertisement message","Using Quark.cc!"));
    private final IntSetting    delay   = register(new IntSetting   ("Delay","Seconds between messages",60,5,300));
    private final TimerUtil timer = new TimerUtil();

    public AutoAd() { super("AutoAd","Sends an advertisement message at intervals",Category.MISC); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null||mc.getNetworkHandler()==null) return;
        if (!timer.hasReached(delay.get()*1000L)) return;
        mc.getNetworkHandler().sendChatMessage(message.get());
        timer.reset();
    }
}
