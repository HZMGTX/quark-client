package com.ghostclient.module.modules.player;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.EnumSetting;
import com.ghostclient.setting.IntSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

/**
 * AutoEat - automatically eats the best food when health or hunger drops below threshold.
 */
public class AutoEat extends Module {

    public enum Priority {
        HEALTH, HUNGER
    }

    private final IntSetting healthThreshold = register(new IntSetting(
            "Health", "Eat when health is at or below this value (half-hearts)", 14, 1, 18));

    private final IntSetting hungerThreshold = register(new IntSetting(
            "Hunger", "Eat when hunger is at or below this value", 15, 1, 20));

    private final BoolSetting instant = register(new BoolSetting(
            "Instant", "Force instant food use (use with FastEat)", false));

    private final EnumSetting<Priority> priority = register(new EnumSetting<>(
            "Priority", "Whether to trigger on Health or Hunger threshold first", Priority.HUNGER));

    // Slot of the food item we moved to the hotbar (-1 = none)
    private int originalHotbarSlot = -1;
    private int savedSlot = -1;
    private boolean eating = false;

    // Items that are NOT worth eating
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
            if (player.getHungerManager().getFoodLevel() <= hungerThreshold.get()) shouldEat = true;
            if (player.getHealth() <= healthThreshold.get()) shouldEat = true;
        } else {
            if (player.getHealth() <= healthThreshold.get()) shouldEat = true;
            if (player.getHungerManager().getFoodLevel() <= hungerThreshold.get()) shouldEat = true;
        }

        if (!shouldEat) {
            // Stop eating and restore slot
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

        // Try to eat with already-held food first
        if (eating && player.isUsingItem()) {
            if (instant.isEnabled()) {
                // Force finish using
                mc.interactionManager.stopUsingItem(player);
            }
            return; // Already eating
        }

        // Find the best food in inventory
        int bestSlot = -1;
        boolean bestIsHotbar = false;
        int bestNutrition = -1;

        // Check hotbar first
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isFood()) continue;
            if (isJunkFood(stack.getItem())) continue;
            FoodComponent food = stack.getItem().getFoodComponent();
            if (food == null) continue;
            int nutrition = food.getHunger();
            if (nutrition > bestNutrition) {
                bestNutrition = nutrition;
                bestSlot = i;
                bestIsHotbar = true;
            }
        }

        // Check main inventory (slots 9-35)
        for (int i = 9; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isFood()) continue;
            if (isJunkFood(stack.getItem())) continue;
            FoodComponent food = stack.getItem().getFoodComponent();
            if (food == null) continue;
            int nutrition = food.getHunger();
            if (nutrition > bestNutrition) {
                bestNutrition = nutrition;
                bestSlot = i;
                bestIsHotbar = false;
            }
        }

        if (bestSlot == -1) return; // No food found

        if (bestIsHotbar) {
            // Just switch to that hotbar slot
            if (savedSlot == -1) {
                savedSlot = player.getInventory().selectedSlot;
            }
            player.getInventory().selectedSlot = bestSlot;
        } else {
            // Move food from inventory to hotbar slot 8 (last slot)
            if (savedSlot == -1) {
                savedSlot = player.getInventory().selectedSlot;
            }
            // Swap inventory slot to hotbar slot 8
            mc.interactionManager.clickSlot(
                    player.currentScreenHandler.syncId,
                    bestSlot, // slot index in container (shifted by 0 in player inv)
                    8, // hotbar slot 8 as swap target
                    SlotActionType.SWAP,
                    player
            );
            player.getInventory().selectedSlot = 8;
        }

        // Start eating
        mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
        mc.options.useKey.setPressed(true);
        eating = true;
    }
}
