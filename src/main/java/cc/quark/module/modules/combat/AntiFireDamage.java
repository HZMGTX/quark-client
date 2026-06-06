package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AntiFireDamage extends Module {
    private final BoolSetting useMilk = register(new BoolSetting("Use Milk", "Drink milk when on fire", true));
    private final BoolSetting useWater = register(new BoolSetting("Use Water", "Use water bucket when on fire", false));

    public AntiFireDamage() { super("AntiFireDamage", "Auto-extinguishes fire using milk or water", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!mc.player.isOnFire()) return;
        var target = useMilk.isEnabled() ? Items.MILK_BUCKET : (useWater.isEnabled() ? Items.WATER_BUCKET : null);
        if (target == null) return;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == target) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = prev;
                return;
            }
        }
    }
}
