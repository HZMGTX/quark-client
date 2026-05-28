package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;

public class FastBow extends Module {

    private final DoubleSetting chargeTicks = register(new DoubleSetting("Charge Ticks", "Ticks before forcing release (3-20)", 10.0, 3.0, 20.0));

    public FastBow() {
        super("FastBow", "Forces bow release after minimal charge for rapid fire", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isUsingItem()) return;

        var activeItem = mc.player.getActiveItem();
        if (!(activeItem.getItem() instanceof BowItem)) return;

        //? if mc >= "1.20.5" {
        int maxUse = activeItem.getMaxUseTime(mc.player);
        //?} else {
        /*int maxUse = activeItem.getItem().getMaxUseTime(activeItem);*/
        //?}
        int used = maxUse - mc.player.getItemUseTimeLeft();

        if (used >= (int) chargeTicks.get()) {
            mc.player.stopUsingItem();
        }
    }

    @Override
    public String getSuffix() {
        return (int) chargeTicks.get() + "t";
    }
}
