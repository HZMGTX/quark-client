package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoRecipe extends Module {

    private final ModeSetting recipe = register(new ModeSetting(
            "Recipe", "Item to craft", "Sticks", "Sticks", "Planks", "Torches"));

    private final TimerUtil timer = new TimerUtil();

    public AutoRecipe() {
        super("AutoRecipe", "Opens workbench and crafts a preconfigured recipe", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        if (!(mc.currentScreen instanceof CraftingScreen)) return;
        if (!(mc.player.currentScreenHandler instanceof CraftingScreenHandler handler)) return;

        String mode = recipe.get();
        switch (mode) {
            case "Sticks"  -> placeRecipeSticks(handler);
            case "Planks"  -> placeRecipePlanks(handler);
            case "Torches" -> placeRecipeTorches(handler);
        }

        mc.interactionManager.clickSlot(handler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
    }

    private void placeRecipeSticks(CraftingScreenHandler handler) {
        fillSlotFromInventory(handler, 1, Items.OAK_PLANKS);
        fillSlotFromInventory(handler, 4, Items.OAK_PLANKS);
    }

    private void placeRecipePlanks(CraftingScreenHandler handler) {
        fillSlotFromInventory(handler, 1, Items.OAK_LOG);
    }

    private void placeRecipeTorches(CraftingScreenHandler handler) {
        fillSlotFromInventory(handler, 1, Items.COAL);
        fillSlotFromInventory(handler, 4, Items.STICK);
    }

    private void fillSlotFromInventory(CraftingScreenHandler handler, int craftingSlotIndex, net.minecraft.item.Item item) {
        if (!handler.getSlot(craftingSlotIndex).getStack().isEmpty()) return;

        for (int i = 10; i < handler.slots.size(); i++) {
            if (handler.getSlot(i).getStack().getItem() == item) {
                mc.interactionManager.clickSlot(handler.syncId, i, craftingSlotIndex, SlotActionType.SWAP, mc.player);
                return;
            }
        }
    }
}
