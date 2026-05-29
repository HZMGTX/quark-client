package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BowItem;

/**
 * FastBow — forces the bow to release after only MinCharge ticks of draw,
 * enabling rapid-fire at the cost of reduced arrow velocity.
 */
public class FastBow extends Module {

    private final IntSetting minCharge = register(new IntSetting(
            "Min Charge", "Ticks of draw before auto-release (20 = full power)", 5, 1, 20));

    public FastBow() {
        super("FastBow", "Forces bow release after minimal charge for rapid fire", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return minCharge.get() + "t";
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

        if (used >= minCharge.get()) {
            mc.player.stopUsingItem();
        }
    }
}
