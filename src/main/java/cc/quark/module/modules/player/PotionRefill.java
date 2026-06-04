package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class PotionRefill extends Module {

    private final BoolSetting strength = register(new BoolSetting(
            "Strength", "Refill strength potions", true));
    private final BoolSetting speed = register(new BoolSetting(
            "Speed", "Refill speed potions", true));
    private final BoolSetting regen = register(new BoolSetting(
            "Regen", "Refill regen potions", true));

    private ItemStack[] prevHotbar = new ItemStack[9];

    public PotionRefill() {
        super("PotionRefill", "Auto-refills potion items from inventory", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        prevHotbar = new ItemStack[9];
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        var inv = mc.player.getInventory();

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack current = inv.getStack(hotbarSlot);
            ItemStack prev = prevHotbar[hotbarSlot];

            if (prev != null && isTrackedPotion(prev) && current.isEmpty()) {
                int replacement = findMatchingPotion(prev, hotbarSlot);
                if (replacement != -1) {
                    mc.interactionManager.clickSlot(
                            mc.player.currentScreenHandler.syncId,
                            replacement,
                            hotbarSlot,
                            SlotActionType.SWAP,
                            mc.player);
                }
            }
            prevHotbar[hotbarSlot] = current.isEmpty() ? ItemStack.EMPTY : current.copy();
        }
    }

    private boolean isTrackedPotion(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() == Items.SPLASH_POTION
                || stack.getItem() == Items.POTION
                || stack.getItem() == Items.LINGERING_POTION;
    }

    private int findMatchingPotion(ItemStack original, int excludeHotbar) {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (i == excludeHotbar) continue;
            ItemStack s = inv.getStack(i);
            if (s.isEmpty() || s.getItem() != original.getItem()) continue;
            // Convert inventory index to screen handler slot
            return i < 9 ? 36 + i : i;
        }
        return -1;
    }
}
