package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class ItemRefill extends Module {

    private final BoolSetting refillArrows = register(new BoolSetting(
            "RefillArrows", "Refill arrows from inventory to hotbar", true));

    private final BoolSetting refillFood = register(new BoolSetting(
            "RefillFood", "Refill food items from inventory to hotbar", true));

    private final BoolSetting refillBlocks = register(new BoolSetting(
            "RefillBlocks", "Refill building blocks from inventory to hotbar", true));

    private int tickDelay = 0;

    public ItemRefill() {
        super("ItemRefill", "Auto-refills items (arrows, food, blocks) from inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        tickDelay++;
        if (tickDelay < 4) return;
        tickDelay = 0;

        var inv = mc.player.getInventory();

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack hotbarStack = inv.getStack(hotbarSlot);
            if (hotbarStack.isEmpty() || hotbarStack.getMaxCount() <= 1) continue;
            if (hotbarStack.getCount() > 1) continue; // Only refill when down to 1

            Item item = hotbarStack.getItem();
            boolean isArrow = item == Items.ARROW || item == Items.SPECTRAL_ARROW || item == Items.TIPPED_ARROW;
            boolean isFood = isFood(hotbarStack);
            boolean isBlock = item instanceof net.minecraft.item.BlockItem;

            if (isArrow && !refillArrows.isEnabled()) continue;
            if (isFood && !refillFood.isEnabled()) continue;
            if (isBlock && !refillBlocks.isEnabled()) continue;
            if (!isArrow && !isFood && !isBlock) continue;

            // Find matching item in inventory
            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack invStack = inv.getStack(invSlot);
                if (invStack.isEmpty() || invStack.getItem() != item) continue;

                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        invSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
                return; // One action per cycle
            }
        }
    }

    private boolean isFood(ItemStack stack) {
        //? if mc >= "1.20.5" {
        return stack.contains(net.minecraft.component.DataComponentTypes.FOOD);
        //?} else {
        /*return stack.getItem().isFood();*/
        //?}
    }
}
