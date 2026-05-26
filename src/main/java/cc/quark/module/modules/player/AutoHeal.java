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
 * AutoHeal - eats a golden apple when health drops below the configured threshold.
 */
public class AutoHeal extends Module {

    private final IntSetting health = register(new IntSetting("Health", "Heal below this health (half-hearts)", 8, 1, 19));

    public AutoHeal() {
        super("AutoHeal", "Eats golden apples when low on health", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > health.get()) return;

        int slot = InventoryUtil.findItem(Items.GOLDEN_APPLE);
        if (slot == -1) slot = InventoryUtil.findItem(Items.ENCHANTED_GOLDEN_APPLE);
        if (slot < 0 || slot >= 9) return;

        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.options.useKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        mc.options.useKey.setPressed(false);
    }
}
