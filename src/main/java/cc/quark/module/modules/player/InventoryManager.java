package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;

/**
 * InventoryManager - manages inventory automatically.
 *
 * Features:
 *   - Auto Refill  — move items from backpack to hotbar when a hotbar slot runs empty
 *   - Auto Stack   — stack items of the same type together
 *   - Drop Trash   — drop junk items automatically
 *   - Sort         — periodically sort inventory by item value
 */
public class InventoryManager extends Module {

    private final BoolSetting autoRefill = register(new BoolSetting(
            "Auto Refill", "Move items from inventory to hotbar when hotbar slot runs empty", true));

    private final BoolSetting autoStack = register(new BoolSetting(
            "Auto Stack", "Automatically stack items of the same type together", true));

    private final BoolSetting dropTrash = register(new BoolSetting(
            "Drop Trash", "Drop junk items (gravel, dirt, rotten flesh, bones, arrows if full)", true));

    private final BoolSetting sort = register(new BoolSetting(
            "Sort", "Periodically sort inventory by category", false));

    private int tickDelay = 0;

    private static final Set<Item> TRASH_ITEMS = new HashSet<>(Arrays.asList(
            Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO,
            Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS,
            Items.BEETROOT_SEEDS, Items.BONE, Items.STRING,
            Items.GRAVEL, Items.DIRT, Items.SAND, Items.COBBLESTONE
    ));

    public InventoryManager() {
        super("InventoryManager", "Auto-manages inventory: refill, stack, drop trash, and sort", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        // Don't run when a screen other than game is open
        if (mc.currentScreen != null) return;

        tickDelay++;
        if (tickDelay < 5) return; // Run every 5 ticks
        tickDelay = 0;

        if (dropTrash.isEnabled()) {
            if (dropJunkItems()) return; // One action per cycle
        }

        if (autoStack.isEnabled()) {
            if (stackItems()) return;
        }

        if (autoRefill.isEnabled()) {
            if (refillHotbar()) return;
        }

        if (sort.isEnabled()) {
            sortInventory();
        }
    }

    // -------------------------------------------------------------------------
    // Junk dropping
    // -------------------------------------------------------------------------

    private boolean dropJunkItems() {
        // Check if arrows slot is full (all 36 slots) — only trash arrows if inventory is very full
        boolean arrowsFull = isInventoryFull();

        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            boolean isTrash = TRASH_ITEMS.contains(stack.getItem());
            boolean isArrow = stack.getItem() == Items.ARROW && arrowsFull;

            if (isTrash || isArrow) {
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i, 0, SlotActionType.THROW, mc.player);
                return true; // One drop per cycle
            }
        }
        return false;
    }

    private boolean isInventoryFull() {
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Auto Stack
    // -------------------------------------------------------------------------

    private boolean stackItems() {
        // Find two stacks of the same item that aren't full and merge them
        for (int i = 9; i < 36; i++) {
            ItemStack a = mc.player.getInventory().getStack(i);
            if (a.isEmpty() || a.getCount() >= a.getMaxCount()) continue;

            for (int j = i + 1; j < 36; j++) {
                ItemStack b = mc.player.getInventory().getStack(j);
                if (b.isEmpty()) continue;
                if (a.getItem() != b.getItem()) continue;
                if (a.getCount() >= a.getMaxCount()) break;

                // Click a onto b to stack (shift-click to auto-stack)
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        j, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        j, 0, SlotActionType.PICKUP, mc.player);
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Auto Refill
    // -------------------------------------------------------------------------

    private boolean refillHotbar() {
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack hotbarStack = mc.player.getInventory().getStack(hotbarSlot);
            if (hotbarStack.isEmpty()) continue;
            // Check if this is a stackable item that's nearly depleted
            if (hotbarStack.getMaxCount() <= 1) continue;
            if (hotbarStack.getCount() > 1) continue;

            // Find matching item in inventory
            Item needed = hotbarStack.getItem();
            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack invStack = mc.player.getInventory().getStack(invSlot);
                if (invStack.isEmpty() || invStack.getItem() != needed) continue;

                // Swap/move from inventory to hotbar
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        invSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Sorting
    // -------------------------------------------------------------------------

    private void sortInventory() {
        // Bubble-sort style single pass each tick by item value
        for (int i = 9; i < 35; i++) {
            ItemStack a = mc.player.getInventory().getStack(i);
            ItemStack b = mc.player.getInventory().getStack(i + 1);
            if (a.isEmpty() || b.isEmpty()) continue;

            if (itemValue(a) < itemValue(b)) {
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
        if (stack.getItem() instanceof SwordItem) return 1000;
        if (stack.getItem() instanceof PickaxeItem) return 900;
        if (stack.getItem() instanceof AxeItem) return 850;
        if (stack.getItem() instanceof ShovelItem) return 800;
        if (stack.getItem() instanceof ArmorItem a) return 700 + a.getProtectionAmount();
        if (stack.getItem() instanceof BowItem) return 600;
        if (stack.getItem() instanceof CrossbowItem) return 590;
        int nutrition = getFoodNutrition(stack);
        if (nutrition > 0) return 400 + nutrition;
        return stack.getCount();
    }

    private int getFoodNutrition(ItemStack stack) {
        //? if mc >= "1.20.5" {
        if (!stack.contains(net.minecraft.component.DataComponentTypes.FOOD)) return 0;
        net.minecraft.component.type.FoodComponent fc = stack.get(net.minecraft.component.DataComponentTypes.FOOD);
        return fc != null ? fc.nutrition() : 0;
        //?} else {
        /*if (!stack.getItem().isFood()) return 0;
        net.minecraft.item.FoodComponent fc = stack.getItem().getFoodComponent();
        return fc != null ? fc.getHunger() : 0;*/
        //?}
    }
}
