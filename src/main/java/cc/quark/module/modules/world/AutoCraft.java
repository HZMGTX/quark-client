package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoCraft extends Module {

    private final ModeSetting recipe = register(new ModeSetting(
            "Recipe", "Recipe to auto-craft",
            "Sticks", "Sticks", "Planks", "Torches", "Chest"));

    private final TimerUtil timer = new TimerUtil();

    public AutoCraft() {
        super("AutoCraft", "Auto-crafts items at crafting table", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen == null) return;
        if (!timer.hasReached(300)) return;
        timer.reset();

        var handler = mc.player.currentScreenHandler;
        if (!(handler instanceof CraftingScreenHandler crafting)) return;

        // Slot 0 is the output slot in a CraftingScreenHandler
        var outputSlot = crafting.slots.get(0);
        if (!outputSlot.hasStack()) {
            setupRecipe(crafting);
            return;
        }

        // Quick-move output to inventory
        mc.interactionManager.clickSlot(crafting.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
    }

    private void setupRecipe(CraftingScreenHandler crafting) {
        // Slots 1-9 are the 3x3 grid
        int totalSlots = crafting.slots.size();
        int playerStart = totalSlots - 36;

        switch (recipe.get()) {
            case "Planks" -> placeIngredient(crafting, 1, net.minecraft.item.Items.OAK_LOG, playerStart);
            case "Sticks" -> {
                placeIngredient(crafting, 1, net.minecraft.item.Items.OAK_PLANKS, playerStart);
                placeIngredient(crafting, 4, net.minecraft.item.Items.OAK_PLANKS, playerStart);
            }
            case "Torches" -> {
                placeIngredient(crafting, 1, net.minecraft.item.Items.COAL, playerStart);
                placeIngredient(crafting, 4, net.minecraft.item.Items.STICK, playerStart);
            }
            case "Chest" -> {
                for (int slot : new int[]{1, 2, 3, 4, 6, 7, 8, 9}) {
                    placeIngredient(crafting, slot, net.minecraft.item.Items.OAK_PLANKS, playerStart);
                }
            }
        }
    }

    private void placeIngredient(CraftingScreenHandler crafting, int gridSlot, net.minecraft.item.Item ingredient, int playerStart) {
        int totalSlots = crafting.slots.size();
        for (int i = playerStart; i < totalSlots; i++) {
            var stack = crafting.slots.get(i).getStack();
            if (!stack.isEmpty() && stack.isOf(ingredient)) {
                // Pick up from inventory
                mc.interactionManager.clickSlot(crafting.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                // Place one in grid slot
                mc.interactionManager.clickSlot(crafting.syncId, gridSlot, 1, SlotActionType.PICKUP, mc.player);
                // Put remainder back
                mc.interactionManager.clickSlot(crafting.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }
    }
}
