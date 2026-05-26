package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * AntiHungerLoss - keeps client-side hunger from dropping below 6 (sprint threshold).
 */
public class AntiHungerLoss extends Module {

    public AntiHungerLoss() {
        super("AntiHungerLoss", "Stops hunger from dropping too low", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.getHungerManager().getFoodLevel() < 7) {
            mc.player.getHungerManager().setFoodLevel(7);
        }
    }
}
