package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.Hand;

/**
 * AntiAFK2 - periodically swings the player's hand to avoid being flagged as AFK.
 */
public class AntiAFK2 extends Module {

    private final IntSetting interval = register(new IntSetting("Interval", "Ticks between swings", 100, 20, 600));
    private int timer = 0;

    public AntiAFK2() {
        super("AntiAFK2", "Swings hand periodically to avoid AFK kicks", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (++timer >= interval.get()) {
            timer = 0;
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
