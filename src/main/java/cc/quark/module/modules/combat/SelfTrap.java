package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * SelfTrap - places obsidian around the player when health gets low.
 */
public class SelfTrap extends Module {

    private final IntSetting health = register(new IntSetting("Health", "Trigger health", 8, 1, 20));

    public SelfTrap() {
        super("SelfTrap", "Traps yourself in obsidian when low", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > health.get()) return;
        if (!mc.player.getMainHandStack().isOf(Items.OBSIDIAN)) return;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
