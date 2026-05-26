package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.Hand;

/**
 * InstantUse - finishes the current item use immediately by spamming interact.
 */
public class InstantUse extends Module {

    public InstantUse() {
        super("InstantUse", "Completes item use instantly", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.isUsingItem() && mc.player.getItemUseTimeLeft() > 0) {
            for (int i = 0; i < 35; i++) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
        }
    }
}
