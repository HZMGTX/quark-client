package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;

public class BowRelease extends Module {

    private final IntSetting minCharge = register(new IntSetting(
            "Min Charge", "Minimum draw ticks before auto-release (20 = full)", 20, 5, 72));

    public BowRelease() {
        super("BowRelease", "Releases bow/crossbow automatically at the configured charge", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isUsingItem()) return;

        ItemStack active = mc.player.getActiveItem();
        boolean isBow = active.getItem() instanceof BowItem || active.getItem() instanceof CrossbowItem;
        if (!isBow) return;

        int elapsed = active.getItem().getMaxUseTime(active) - mc.player.getItemUseTimeLeft();
        if (elapsed >= minCharge.get()) {
            mc.player.stopUsingItem();
        }
    }
}
