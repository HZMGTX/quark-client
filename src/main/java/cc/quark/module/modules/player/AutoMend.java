package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoMend - throws experience bottles to repair Mending gear when the held item is damaged.
 */
public class AutoMend extends Module {

    private final IntSetting minDamage = register(new IntSetting("MinDamage", "Throw when held item damage exceeds this", 50, 1, 1000));

    public AutoMend() {
        super("AutoMend", "Throws XP bottles to mend damaged gear", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getMainHandStack().isEmpty() || !mc.player.getMainHandStack().isDamageable()) return;
        if (mc.player.getMainHandStack().getDamage() < minDamage.get()) return;
        if (InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE) == -1) return;

        int slot = InventoryUtil.findItem(Items.EXPERIENCE_BOTTLE);
        if (slot >= 0 && slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
