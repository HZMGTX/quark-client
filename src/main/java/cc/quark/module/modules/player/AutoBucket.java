package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoBucket - places a water bucket when falling to break fall damage.
 */
public class AutoBucket extends Module {

    private final BoolSetting onlyFalling = register(new BoolSetting("OnlyFalling", "Only when falling fast", true));

    public AutoBucket() {
        super("AutoBucket", "Auto-places a water bucket to save from falls", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (onlyFalling.isEnabled() && mc.player.fallDistance < 5.0f) return;
        int slot = InventoryUtil.findItem(Items.WATER_BUCKET);
        if (slot < 0 || slot >= 9) return;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }
}
