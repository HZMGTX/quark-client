package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * FoodTweaks - Automatically eats food at a configurable hunger level,
 * optionally preferring golden food (golden apple, golden carrot) for
 * the absorption / regen effects.
 */
public class FoodTweaks extends Module {

    private final IntSetting hungerThreshold = register(new IntSetting(
            "Hunger Threshold", "Auto-eat when food bar is at or below this value", 16, 1, 20));

    private final BoolSetting preferGolden = register(new BoolSetting(
            "Prefer Golden", "Prefer golden food (golden apple, golden carrot) when available", true));

    private final BoolSetting ignoreJunk = register(new BoolSetting(
            "Ignore Junk", "Skip rotten flesh, spider eyes, poisonous potatoes, pufferfish", true));

    private final BoolSetting eatInCombat = register(new BoolSetting(
            "Eat In Combat", "Eat even while attacking (may interrupt attacks)", false));

    private boolean wasEating = false;
    private int savedSlot = -1;

    private static boolean isGoldenFood(Item item) {
        return item == Items.GOLDEN_APPLE
                || item == Items.ENCHANTED_GOLDEN_APPLE
                || item == Items.GOLDEN_CARROT;
    }

    private static boolean isJunkFood(Item item) {
        return item == Items.ROTTEN_FLESH
                || item == Items.SPIDER_EYE
                || item == Items.POISONOUS_POTATO
                || item == Items.PUFFERFISH;
    }

    public FoodTweaks() {
        super("FoodTweaks", "Auto-eats at configurable hunger level, prefers golden food", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (wasEating && mc.player != null) {
            mc.options.useKey.setPressed(false);
            wasEating = false;
            if (savedSlot != -1) {
                mc.player.getInventory().selectedSlot = savedSlot;
                savedSlot = -1;
            }
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        int food = mc.player.getHungerManager().getFoodLevel();
        if (food > hungerThreshold.get()) {
            if (wasEating) {
                mc.options.useKey.setPressed(false);
                wasEating = false;
                if (savedSlot != -1) {
                    mc.player.getInventory().selectedSlot = savedSlot;
                    savedSlot = -1;
                }
            }
            return;
        }

        if (!eatInCombat.isEnabled() && mc.player.handSwinging) return;
        if (mc.player.isUsingItem()) return;

        // Find best food in hotbar
        int bestSlot = -1;
        boolean bestIsGolden = false;
        int bestNutrition = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!stack.contains(DataComponentTypes.FOOD)) continue;
            FoodComponent fc = stack.get(DataComponentTypes.FOOD);
            if (fc == null) continue;
            if (ignoreJunk.isEnabled() && isJunkFood(stack.getItem())) continue;

            boolean golden = isGoldenFood(stack.getItem());
            int nutrition = fc.nutrition();

            if (preferGolden.isEnabled() && golden && !bestIsGolden) {
                // Always prefer golden over non-golden
                bestSlot = i;
                bestIsGolden = true;
                bestNutrition = nutrition;
            } else if (preferGolden.isEnabled() && !golden && bestIsGolden) {
                // Already have golden, skip non-golden
                continue;
            } else if (nutrition > bestNutrition) {
                bestSlot = i;
                bestNutrition = nutrition;
                bestIsGolden = golden;
            }
        }

        if (bestSlot == -1) return;

        if (savedSlot == -1) savedSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = bestSlot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.options.useKey.setPressed(true);
        wasEating = true;
    }
}
