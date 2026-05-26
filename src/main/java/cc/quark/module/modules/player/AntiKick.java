package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.Hand;

/**
 * AntiKick - sends periodic activity to avoid idle-kick timeouts.
 */
public class AntiKick extends Module {

    private final IntSetting interval = register(new IntSetting("Interval", "Ticks between actions", 200, 40, 1200));
    private int timer = 0;

    public AntiKick() {
        super("AntiKick", "Prevents idle kicks by sending activity", Category.PLAYER);
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
