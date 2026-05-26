package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * GapAura - eats a held golden apple while in combat when health is low.
 */
public class GapAura extends Module {

    private final IntSetting health = register(new IntSetting("Health", "Trigger health", 12, 1, 20));

    public GapAura() {
        super("GapAura", "Eats a golden apple when health is low", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > health.get()) return;
        boolean hasGap = mc.player.getMainHandStack().isOf(Items.GOLDEN_APPLE)
                || mc.player.getMainHandStack().isOf(Items.ENCHANTED_GOLDEN_APPLE);
        if (!hasGap) return;
        if (!mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
