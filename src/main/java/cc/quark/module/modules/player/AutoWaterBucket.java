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
 * AutoWaterBucket - moves a water bucket to the hotbar so it is always ready.
 */
public class AutoWaterBucket extends Module {

    private final IntSetting hotbar = register(new IntSetting("HotbarSlot", "Hotbar slot to keep the bucket", 8, 0, 8));

    public AutoWaterBucket() {
        super("AutoWaterBucket", "Keeps a water bucket on the hotbar", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (InventoryUtil.findItem(Items.WATER_BUCKET) == hotbar.get()) return;
        int slot = InventoryUtil.findItem(Items.WATER_BUCKET);
        if (slot >= 9) {
            InventoryUtil.moveToHotbar(slot, hotbar.get());
        }
    }
}
