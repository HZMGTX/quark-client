package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;

/**
 * BowRelease — auto-releases the bow after the configured number of charge
 * ticks (20 = full power, 5 = minimum).  Works for bows and crossbows.
 */
public class BowRelease extends Module {

    private final IntSetting minCharge = register(new IntSetting(
            "Min Charge", "Draw ticks before auto-release (20 = full power)", 20, 5, 72));

    public BowRelease() {
        super("BowRelease", "Auto-releases bow or crossbow at the configured charge", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return minCharge.get() + "t";
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isUsingItem()) return;

        ItemStack active = mc.player.getActiveItem();
        boolean isBow      = active.getItem() instanceof BowItem;
        boolean isCrossbow = active.getItem() instanceof CrossbowItem;
        if (!isBow && !isCrossbow) return;

        //? if mc >= "1.20.5" {
        int elapsed = active.getItem().getMaxUseTime(active, mc.player) - mc.player.getItemUseTimeLeft();
        //?} else {
        /*int elapsed = active.getItem().getMaxUseTime(active) - mc.player.getItemUseTimeLeft();*/
        //?}
        if (elapsed >= minCharge.get()) {
            mc.player.stopUsingItem();
        }
    }
}
