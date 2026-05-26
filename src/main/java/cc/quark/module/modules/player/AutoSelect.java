package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.InventoryUtil;

/**
 * AutoSelect - selects the best sword on the hotbar automatically.
 */
public class AutoSelect extends Module {

    public AutoSelect() {
        super("AutoSelect", "Selects the best weapon on the hotbar", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        int slot = InventoryUtil.findBestSword();
        if (slot >= 0 && slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }
}
