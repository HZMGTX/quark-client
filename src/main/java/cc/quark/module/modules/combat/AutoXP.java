package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.Hand;

/**
 * AutoXP - throws XP bottles on a timer to mend gear.
 */
public class AutoXP extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between throws", 4, 1, 40));

    private int ticks;

    public AutoXP() {
        super("AutoXP", "Throws experience bottles automatically", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        ticks++;
        if (ticks < delay.get()) return;
        ticks = 0;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
