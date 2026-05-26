package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.*;
import net.minecraft.util.Hand;

public class AutoPotion extends Module {

    private final DoubleSetting minHp = register(new DoubleSetting(
            "Min HP", "Use a health potion when HP falls below this (hearts)", 8.0, 1.0, 19.0));
    private final DoubleSetting minFood = register(new DoubleSetting(
            "Min Food", "Use a food item when food drops below this", 6.0, 0.0, 20.0));

    private int cooldown = 0;

    public AutoPotion() {
        super("AutoPotion", "Automatically uses health potions and food when stats drop below thresholds", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (--cooldown > 0) return;

        float hp   = mc.player.getHealth();
        int   food = mc.player.getHungerManager().getFoodLevel();

        var inv = mc.player.getInventory();
        int prev = inv.selectedSlot;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            Item item = stack.getItem();

            boolean isHealthPotion = item instanceof SplashPotionItem || item instanceof PotionItem;
            //? if mc >= "1.20.5" {
            boolean isFoodItem = stack.contains(net.minecraft.component.DataComponentTypes.FOOD);
            //?} else {
            /*boolean isFoodItem = stack.getItem().isFood();*/
            //?}

            if (isHealthPotion && hp < (float) (minHp.get() * 2)) {
                inv.selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                inv.selectedSlot = prev;
                cooldown = 20;
                return;
            }

            if (isFoodItem && food < (int) minFood.get()
                    && !mc.player.isUsingItem()) {
                inv.selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                inv.selectedSlot = prev;
                cooldown = 5;
                return;
            }
        }
    }
}
