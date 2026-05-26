package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoCev - automatically breaks out by mining when trapped and low on health.
 */
public class AutoCev extends Module {

    private final IntSetting health = register(new IntSetting("Health", "Trigger health", 6, 1, 20));

    public AutoCev() {
        super("AutoCev", "Eats and escapes when trapped", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > health.get()) return;
        if (mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
