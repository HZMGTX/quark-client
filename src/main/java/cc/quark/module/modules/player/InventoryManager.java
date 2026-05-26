package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;

/**
 * InventoryManager - sorts inventory, drops junk, and organizes the hotbar.
 *
 * Sort modes:
 *   NAME     â€“ alphabetical by item name
 *   STACK    â€“ group by item type (largest stacks first)
 *   VALUE    â€“ tool/armor quality, then food, then materials
 */
public class InventoryManager extends Module {

    public enum SortMode {
        NAME, STACK, VALUE
    }

    private final EnumSetting<SortMode> sortMode = register(new EnumSetting<>(
            "Sort Mode", "How to sort inventory items", SortMode.STACK));

    private final BoolSetting dropJunk = register(new BoolSetting(
            "Drop Junk", "Drop junk items (rotten flesh, seeds, etc.)", true));

    private final BoolSetting hotbarFill = register(new BoolSetting(
            "Hotbar Fill", "Move best weapons/tools/food to hotbar slots", true));

    private int tickDelay = 0;

    public InventoryManager() {
        super("InventoryManager", "Sorts inventory, drops junk, and organizes hotbar", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        // Don't run when a screen other than game is open
        if (mc.currentScreen != null) return;

        tickDelay++;
        if (tickDelay < 5) return; // Run every 5 ticks
        tickDelay = 0;

        if (dropJunk.isEnabled()) {
            dropJunkItems();
        }

        if (hotbarFill.isEnabled()) {
            fillHotbar();
        }

        sortInventory();
    }

    // -------------------------------------------------------------------------
    // Junk dropping
    // -------------------------------------------------------------------------

    private static final Set<Item> JUNK_ITEMS = new HashSet<>(Arrays.asList(
            Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO,
            Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS,
            Items.BEETROOT_SEEDS, Items.BONE, Items.STRING,
            Items.COBBLESTONE, Items.DIRT, Items.GRAVEL, Items.SAND
    ));

    private void dropJunkItems() {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (JUNK_ITEMS.contains(stack.getItem())) {
                // Drop by clicking outside the inventory window (slot -999 = throw)
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i, 0, SlotActionType.THROW, mc.player);
                return; // One per tick cycle
            }
        }
    }

    // -------------------------------------------------------------------------
    // Hotbar filling
    // -------------------------------------------------------------------------

    private void fillHotbar() {
        // Slot 0: best sword, Slot 1: best pickaxe, Slot 2: best axe,
        // Slot 3: best food, Slot 4-8: fill with remaining valuable items

        int[] desiredSlots = {0, 1, 2, 3};
        Class<?>[] toolTypes = {SwordItem.class, PickaxeItem.class, AxeItem.class, null /* food */};

        for (int i = 0; i < desiredSlots.length; i++) {
            int hotbarSlot = desiredSlots[i];
            ItemStack current = mc.player.getInventory().getStack(hotbarSlot);

            Class<?> toolType = toolTypes[i];
            int bestInvSlot = -1;

            if (toolType != null) {
                // Find best tool of this type in inventory (slots 9-35)
                int bestDamage = -1;
                for (int j = 9; j < 36; j++) {
                    ItemStack stack = mc.player.getInventory().getStack(j);
                    if (!toolType.isInstance(stack.getItem())) continue;
                    if (stack.getDamage() > bestDamage) {
                        bestDamage = stack.getDamage();
                        bestInvSlot = j;
                    }
                }
                // Swap if found and hotbar slot doesn't already have the right type
                if (bestInvSlot != -1 && !toolType.isInstance(current.getItem())) {
                    mc.interactionManager.clickSlot(
                            mc.player.playerScreenHandler.syncId,
                            bestInvSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
                }
            } else {
                // Food slot: find best food
                if (!current.contains(net.minecraft.component.DataComponentTypes.FOOD)) {
                    int bestNutrition = -1;
                    for (int j = 9; j < 36; j++) {
                        ItemStack stack = mc.player.getInventory().getStack(j);
                        if (!stack.contains(net.minecraft.component.DataComponentTypes.FOOD)) continue;
                        net.minecraft.component.type.FoodComponent food = stack.get(net.minecraft.component.DataComponentTypes.FOOD);
                        if (food == null) continue;
                        if (food.nutrition() > bestNutrition) {
                            bestNutrition = food.nutrition();
                            bestInvSlot = j;
                        }
                    }
                    if (bestInvSlot != -1) {
                        mc.interactionManager.clickSlot(
                                mc.player.playerScreenHandler.syncId,
                                bestInvSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Sorting
    // -------------------------------------------------------------------------

    private void sortInventory() {
        // Collect all items from inventory slots 9-35
        List<ItemStack> items = new ArrayList<>();
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                items.add(stack.copy());
            }
        }

        if (items.isEmpty()) return;

        // Sort according to mode
        switch (sortMode.get()) {
            case NAME -> items.sort(Comparator.comparing(s -> s.getItem().toString()));
            case STACK -> items.sort((a, b) -> {
                // Same item type: sort descending by count
                if (a.getItem() == b.getItem()) return b.getCount() - a.getCount();
                return a.getItem().toString().compareTo(b.getItem().toString());
            });
            case VALUE -> items.sort((a, b) -> itemValue(b) - itemValue(a));
        }

        // Write sorted items back via swap operations (simplified: just one pass)
        // This is complex to do perfectly without a real sorting algorithm via packet clicks.
        // We do a bubble-sort style single pass each tick.
        for (int i = 9; i < 35; i++) {
            ItemStack a = mc.player.getInventory().getStack(i);
            ItemStack b = mc.player.getInventory().getStack(i + 1);
            if (a.isEmpty() || b.isEmpty()) continue;

            boolean shouldSwap = false;
            switch (sortMode.get()) {
                case NAME -> shouldSwap = a.getItem().toString().compareTo(b.getItem().toString()) > 0;
                case STACK -> {
                    if (a.getItem() == b.getItem()) {
                        shouldSwap = a.getCount() < b.getCount();
                    } else {
                        shouldSwap = a.getItem().toString().compareTo(b.getItem().toString()) > 0;
                    }
                }
                case VALUE -> shouldSwap = itemValue(a) < itemValue(b);
            }

            if (shouldSwap) {
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i + 1, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i, 0, SlotActionType.PICKUP, mc.player);
                break; // Only one swap per tick
            }
        }
    }

    private int itemValue(ItemStack stack) {
        if (stack.getItem() instanceof net.minecraft.item.SwordItem s) return 1000;
        if (stack.getItem() instanceof PickaxeItem) return 900;
        if (stack.getItem() instanceof AxeItem) return 850;
        if (stack.getItem() instanceof ShovelItem) return 800;
        if (stack.getItem() instanceof ArmorItem a) return 700 + a.getProtection();
        if (stack.getItem() instanceof BowItem) return 600;
        if (stack.getItem() instanceof CrossbowItem) return 590;
        if (stack.contains(net.minecraft.component.DataComponentTypes.FOOD)) {
            net.minecraft.component.type.FoodComponent food = stack.get(net.minecraft.component.DataComponentTypes.FOOD);
            return food != null ? 400 + food.nutrition() : 400;
        }
        return stack.getCount();
    }
}
