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
 * AutoSoup2 - eats mushroom stew when health drops below a threshold.
 */
public class AutoSoup2 extends Module {

    private final IntSetting health = register(new IntSetting("Health", "Use below this health", 14, 1, 19));

    public AutoSoup2() {
        super("AutoSoup2", "Eats soup when low on health", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > health.get()) return;
        int slot = InventoryUtil.findItem(Items.MUSHROOM_STEW);
        if (slot < 0 || slot >= 9) return;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }
}
