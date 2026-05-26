package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.InventoryUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoLava - quickly places and picks up lava with a lava bucket when triggered.
 */
public class AutoLava extends Module {

    public AutoLava() {
        super("AutoLava", "Uses a lava bucket on demand", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        int slot = InventoryUtil.findItem(Items.LAVA_BUCKET);
        if (slot < 0 || slot >= 9) return;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }
}
