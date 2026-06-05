package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import cc.quark.util.TimerUtil;

public class AntiWeakness extends Module {
    private final BoolSetting miningFatigue = register(new BoolSetting("Mining Fatigue", "Drink milk to remove mining fatigue", true));
    private final BoolSetting weakness = register(new BoolSetting("Weakness", "Drink milk to remove weakness", false));
    private final TimerUtil timer = new TimerUtil();

    public AntiWeakness() { super("AntiWeakness", "Drinks milk to remove debuffs like Mining Fatigue", Category.COMBAT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || !timer.hasReached(2000)) return;
        boolean shouldDrink = false;
        if (miningFatigue.isEnabled() && mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) shouldDrink = true;
        if (weakness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) shouldDrink = true;
        if (!shouldDrink) return;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.MILK_BUCKET) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = prev;
                timer.reset();
                return;
            }
        }
    }
}
