package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AntiPoison2 extends Module {

    private final DoubleSetting hpThreshold = register(new DoubleSetting(
            "HP Threshold", "Health below which to consume cure (half-hearts)", 10.0, 2.0, 20.0));

    public AntiPoison2() {
        super("AntiPoison2", "Cancels poison damage with milk/honey", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean poisoned = mc.player.hasStatusEffect(StatusEffects.POISON)
                || mc.player.hasStatusEffect(StatusEffects.WITHER);
        if (!poisoned) return;
        if (mc.player.getHealth() > hpThreshold.get()) return;

        // Search hotbar for milk bucket or honey bottle
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.MILK_BUCKET || stack.getItem() == Items.HONEY_BOTTLE) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = prev;
                break;
            }
        }
    }
}
