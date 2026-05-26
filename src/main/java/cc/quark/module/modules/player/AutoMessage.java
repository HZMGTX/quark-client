package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

/**
 * AutoMessage - sends an advertisement message on a fixed interval.
 */
public class AutoMessage extends Module {

    private final IntSetting interval = register(new IntSetting("Interval", "Ticks between messages", 1200, 200, 12000));
    private int timer = 0;

    public AutoMessage() {
        super("AutoMessage", "Auto-sends a configurable advertisement message", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (++timer >= interval.get()) {
            timer = 0;
            ChatUtil.send("Powered by Quark client");
        }
    }
}
