package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.Hand;

/**
 * NoMissDelay - swings the hand every tick so missed clicks never stall.
 */
public class NoMissDelay extends Module {

    public NoMissDelay() {
        super("NoMissDelay", "Removes the swing delay after a missed click", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.options != null && mc.options.attackKey.isPressed() && mc.crosshairTarget == null) {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
