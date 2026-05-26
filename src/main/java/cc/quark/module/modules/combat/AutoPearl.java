package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoPearl - throws an ender pearl to escape when health is critically low.
 */
public class AutoPearl extends Module {

    private final IntSetting health = register(new IntSetting("Health", "Trigger health", 5, 1, 20));

    public AutoPearl() {
        super("AutoPearl", "Throws an ender pearl when low on health", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > health.get()) return;
        if (!mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)) return;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
