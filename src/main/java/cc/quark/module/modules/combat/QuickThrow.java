package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.Hand;

/**
 * QuickThrow - rapidly throws the held item (potions, pearls, eggs).
 */
public class QuickThrow extends Module {

    private final IntSetting amount = register(new IntSetting("Amount", "Throws per activation", 1, 1, 10));

    public QuickThrow() {
        super("QuickThrow", "Rapidly throws the held item", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.interactionManager == null) {
            toggle();
            return;
        }
        for (int i = 0; i < amount.get(); i++) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        toggle();
    }

    @EventHandler
    public void onTick(EventTick event) {
        // Action runs on enable; nothing per-tick.
    }
}
