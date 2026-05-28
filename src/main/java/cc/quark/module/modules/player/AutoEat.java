package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
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

    private final DoubleSetting healthThreshold = register(new DoubleSetting(
            "Health Threshold", "Eat when health is at or below this value (hearts)", 14.0, 1.0, 20.0));

    private final IntSetting hungerThreshold = register(new IntSetting(
            "Hunger Threshold", "Eat when hunger is at or below this value", 17, 1, 20));

    private final BoolSetting eatBest = register(new BoolSetting(
            "Eat Best", "Prefer the highest nutrition food available", true));

    private final BoolSetting switchBack = register(new BoolSetting(
            "Switch Back", "Restore previous slot after eating", true));

    private final BoolSetting stopSprint = register(new BoolSetting(
            "Stop Sprint", "Stop sprinting while eating (reduces hunger waste)", false));

    private int savedSlot = -1;
    private boolean isEating = false;

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
        if (isEating && mc.player != null) {
            mc.options.useKey.setPressed(false);
            isEating = false;
            if (savedSlot != -1 && switchBack.isEnabled()) {
                mc.player.getInventory().selectedSlot = savedSlot;
            }
            savedSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ClientPlayerEntity player = mc.player;

        boolean shouldEat = player.getHungerManager().getFoodLevel() <= hungerThreshold.get()
                || player.getHealth() <= (float) healthThreshold.get();

        if (!shouldEat) {
            if (isEating) {
                mc.options.useKey.setPressed(false);
                isEating = false;
                if (savedSlot != -1 && switchBack.isEnabled()) {
                    player.getInventory().selectedSlot = savedSlot;
                }
                savedSlot = -1;
            }
            return;
        }

        if (player.isUsingItem()) {
            // Stop sprint while eating if enabled
            if (stopSprint.isEnabled()) {
                player.setSprinting(false);
            }
            return;
        }

        int bestSlot = -1;
        boolean bestIsHotbar = false;
        int bestNutrition = -1;

        if (eatBest.isEnabled()) {
            // Search hotbar first
            for (int i = 0; i < 9; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                int nutrition = getFoodNutrition(stack);
                if (nutrition <= 0 || isJunkFood(stack.getItem())) continue;
                if (nutrition > bestNutrition) {
                    bestNutrition = nutrition;
                    bestSlot = i;
                    bestIsHotbar = true;
                }
            }
            // Then search inventory (slots 9-35)
            for (int i = 9; i < 36; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                int nutrition = getFoodNutrition(stack);
                if (nutrition <= 0 || isJunkFood(stack.getItem())) continue;
                if (nutrition > bestNutrition) {
                    bestNutrition = nutrition;
                    bestSlot = i;
                    bestIsHotbar = false;
                }
            }
        } else {
            // Find first food in hotbar
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

        if (savedSlot == -1) savedSlot = player.getInventory().selectedSlot;

        if (bestIsHotbar) {
            player.getInventory().selectedSlot = bestSlot;
        } else {
            // Swap from inventory to hotbar slot 8
            mc.interactionManager.clickSlot(
                    player.currentScreenHandler.syncId,
                    bestSlot,
                    8,
                    SlotActionType.SWAP,
                    player);
            player.getInventory().selectedSlot = 8;
        }

        // Stop sprint while eating if enabled
        if (stopSprint.isEnabled()) {
            player.setSprinting(false);
        }

        mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
        mc.options.useKey.setPressed(true);
        isEating = true;
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
