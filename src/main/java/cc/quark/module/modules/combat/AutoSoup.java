package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.Hand;

/**
 * AutoSoup - consumes the held soup when health drops below a threshold.
 */
public class AutoSoup extends Module {

    private final DoubleSetting health = register(new DoubleSetting("Health", "Health to eat at", 12.0, 1.0, 20.0));

    public AutoSoup() {
        super("AutoSoup", "Eats soup when low on health", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > health.get()) return;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
