package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

/**
 * SpamBot2 - repeatedly sends a message at a configurable rate.
 */
public class SpamBot2 extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between messages", 40, 5, 200));
    private int timer = 0;

    public SpamBot2() {
        super("SpamBot2", "Spams a configurable chat message", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (++timer >= delay.get()) {
            timer = 0;
            ChatUtil.send("Quark > all");
        }
    }
}
