package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.InventoryUtil;
import net.minecraft.item.ItemStack;

/**
 * HotbarReplenish - refills the selected hotbar stack from inventory when it runs low.
 */
public class HotbarReplenish extends Module {

    public HotbarReplenish() {
        super("HotbarReplenish", "Refills the held stack when low", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        int sel = mc.player.getInventory().selectedSlot;
        ItemStack held = mc.player.getInventory().getStack(sel);
        if (held.isEmpty() || held.getCount() > 8) return;
        int source = InventoryUtil.findItem(held.getItem());
        if (source >= 9) {
            InventoryUtil.moveToHotbar(source, sel);
        }
    }
}
