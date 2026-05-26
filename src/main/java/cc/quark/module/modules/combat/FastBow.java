package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BowItem;

/**
 * FastBow - releases a drawn bow once it has been charged briefly.
 */
public class FastBow extends Module {

    private final IntSetting charge = register(new IntSetting("Charge", "Ticks to charge before release", 5, 0, 20));

    public FastBow() {
        super("FastBow", "Quickly fires drawn bows", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isUsingItem()) return;
        if (!(mc.player.getActiveItem().getItem() instanceof BowItem)) return;
        //? if mc >= "1.20.5" {
        int used = mc.player.getActiveItem().getMaxUseTime(mc.player) - mc.player.getItemUseTimeLeft();
        //?} else {
        /*int used = mc.player.getActiveItem().getItem().getMaxUseTime(mc.player.getActiveItem()) - mc.player.getItemUseTimeLeft();*/
        //?}
        if (used >= charge.get()) {
            mc.player.stopUsingItem();
        }
    }
}
