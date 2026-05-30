package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class PotionRefill extends Module {

    private final BoolSetting splashOnly = register(new BoolSetting(
            "SplashOnly", "Only refill splash potions (not lingering)", true));
    private final IntSetting searchRange = register(new IntSetting(
            "SearchRange", "How many inventory slots to search (9=hotbar only)", 36, 9, 36));

    private ItemStack[] prevHotbar = new ItemStack[9];

    public PotionRefill() {
        super("PotionRefill", "Automatically refills potions from inventory when a hotbar slot becomes empty", Category.PLAYER);
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

            if (prev != null && isPotion(prev) && current.isEmpty()) {
                // Find replacement in inventory
                int replacement = findPotion(prev, hotbarSlot);
                if (replacement != -1) {
                    mc.interactionManager.clickSlot(
                            mc.player.currentScreenHandler.syncId,
                            replacement,
                            hotbarSlot,
                            SlotActionType.SWAP,
                            mc.player);
                }
            }
            prevHotbar[hotbarSlot] = current.copy();
        }
    }

    private boolean isPotion(ItemStack stack) {
        if (stack.getItem() == Items.SPLASH_POTION) return true;
        if (!splashOnly.isEnabled() && stack.getItem() == Items.LINGERING_POTION) return true;
        return false;
    }

    private int findPotion(ItemStack original, int excludeHotbarSlot) {
        var inv = mc.player.getInventory();
        int maxSearch = searchRange.get();
        for (int i = 0; i < maxSearch; i++) {
            if (i == excludeHotbarSlot) continue;
            ItemStack stack = inv.getStack(i);
            if (!isPotion(stack)) continue;
            if (stack.getItem() == original.getItem()) {
                // Convert inventory index to screen handler slot
                if (i < 9) {
                    // hotbar slots in screen handler are 36-44
                    return 36 + i;
                } else {
                    // main inventory slots 9-35 map to screen handler 9-35
                    return i;
                }
            }
        }
        return -1;
    }
}
