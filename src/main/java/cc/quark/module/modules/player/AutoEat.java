package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.client.network.ClientPlayerEntity;
//? if mc >= "1.20.5" {
import net.minecraft.component.type.FoodComponent;
//?}
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoEat extends Module {

    public enum Priority {
        HEALTH, HUNGER
    }

    private final DoubleSetting eatAtHealth = register(new DoubleSetting(
            "Eat At Health", "Eat when health is at or below this value (hearts)", 7.0, 1.0, 20.0));

    private final DoubleSetting eatAtHunger = register(new DoubleSetting(
            "Eat At Hunger", "Eat when hunger is at or below this value", 15.0, 1.0, 20.0));

    private final BoolSetting bestFood = register(new BoolSetting(
            "Best Food", "Prioritize the most nutritious food", true));

    private final BoolSetting instant = register(new BoolSetting(
            "Instant", "Force instant food use (use with FastEat)", false));

    private final EnumSetting<Priority> priority = register(new EnumSetting<>(
            "Priority", "Whether to trigger on Health or Hunger threshold first", Priority.HUNGER));

    private int savedSlot = -1;
    private boolean eating = false;

    private static boolean isJunkFood(Item item) {
        return item == Items.ROTTEN_FLESH
                || item == Items.SPIDER_EYE
                || item == Items.POISONOUS_POTATO
                || item == Items.PUFFERFISH;
    }

    public AutoEat() {
        super("AutoEat", "Auto-eats best food when health/hunger threshold is met", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (eating && mc.player != null) {
            mc.options.useKey.setPressed(false);
            eating = false;
            if (savedSlot != -1) {
                mc.player.getInventory().selectedSlot = savedSlot;
                savedSlot = -1;
            }
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ClientPlayerEntity player = mc.player;

        boolean shouldEat = false;

        if (priority.get() == Priority.HUNGER) {
            if (player.getHungerManager().getFoodLevel() <= (int) eatAtHunger.get()) shouldEat = true;
            if (player.getHealth() <= (float) eatAtHealth.get()) shouldEat = true;
        } else {
            if (player.getHealth() <= (float) eatAtHealth.get()) shouldEat = true;
            if (player.getHungerManager().getFoodLevel() <= (int) eatAtHunger.get()) shouldEat = true;
        }

        if (!shouldEat) {
            if (eating) {
                mc.options.useKey.setPressed(false);
                eating = false;
                if (savedSlot != -1) {
                    player.getInventory().selectedSlot = savedSlot;
                    savedSlot = -1;
                }
            }
            return;
        }

        if (player.isUsingItem()) {
            if (instant.isEnabled()) {
                mc.interactionManager.stopUsingItem(player);
            }
            return;
        }

        int bestSlot = -1;
        boolean bestIsHotbar = false;
        int bestNutrition = -1;

        if (bestFood.isEnabled()) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                int nutrition = getFoodNutrition(stack);
                if (nutrition <= 0 || isJunkFood(stack.getItem())) continue;
                if (nutrition > bestNutrition) { bestNutrition = nutrition; bestSlot = i; bestIsHotbar = true; }
            }
            for (int i = 9; i < 36; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                int nutrition = getFoodNutrition(stack);
                if (nutrition <= 0 || isJunkFood(stack.getItem())) continue;
                if (nutrition > bestNutrition) { bestNutrition = nutrition; bestSlot = i; bestIsHotbar = false; }
            }
        } else {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                int nutrition = getFoodNutrition(stack);
                if (nutrition <= 0 || isJunkFood(stack.getItem())) continue;
                bestSlot = i;
                bestIsHotbar = true;
                break;
            }
        }

        if (bestSlot == -1) return;

        if (bestIsHotbar) {
            if (savedSlot == -1) savedSlot = player.getInventory().selectedSlot;
            player.getInventory().selectedSlot = bestSlot;
        } else {
            if (savedSlot == -1) savedSlot = player.getInventory().selectedSlot;
            mc.interactionManager.clickSlot(
                    player.currentScreenHandler.syncId,
                    bestSlot,
                    8,
                    SlotActionType.SWAP,
                    player);
            player.getInventory().selectedSlot = 8;
        }

        mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
        mc.options.useKey.setPressed(true);
        eating = true;
    }

    private int getFoodNutrition(ItemStack stack) {
        //? if mc >= "1.20.5" {
        if (!stack.contains(net.minecraft.component.DataComponentTypes.FOOD)) return 0;
        FoodComponent fc = stack.get(net.minecraft.component.DataComponentTypes.FOOD);
        return fc != null ? fc.nutrition() : 0;
        //?} else {
        /*if (!stack.getItem().isFood()) return 0;
        net.minecraft.item.FoodComponent fc = stack.getItem().getFoodComponent();
        return fc != null ? fc.getHunger() : 0;*/
        //?}
    }
}
