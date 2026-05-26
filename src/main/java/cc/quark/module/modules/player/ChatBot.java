package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

/**
 * ChatBot - periodically sends a configured greeting message into chat.
 */
public class ChatBot extends Module {

    private final IntSetting interval = register(new IntSetting("Interval", "Ticks between messages", 600, 100, 6000));
    private int timer = 0;

    public ChatBot() {
        super("ChatBot", "Sends a recurring chat message", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (++timer >= interval.get()) {
            timer = 0;
            ChatUtil.send("Hello from Quark!");
        }
    }
}
