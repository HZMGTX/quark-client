package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class HandRefill extends Module {

    private final BoolSetting mainHand = register(new BoolSetting(
            "MainHand", "Auto-switch main hand to similar item when depleted", true));

    private final BoolSetting offHand = register(new BoolSetting(
            "OffHand", "Auto-switch off hand to similar item when depleted", true));

    private Item lastMainHandItem = null;
    private Item lastOffHandItem  = null;

    public HandRefill() {
        super("HandRefill", "Auto-switches to similar item when current runs out", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        var inv = mc.player.getInventory();

        // Main hand
        if (mainHand.isEnabled()) {
            ItemStack mainStack = mc.player.getMainHandStack();
            if (mainStack.isEmpty() && lastMainHandItem != null) {
                refillSlot(inv.selectedSlot, lastMainHandItem);
            } else if (!mainStack.isEmpty()) {
                lastMainHandItem = mainStack.getItem();
            }
        }

        // Off hand
        if (offHand.isEnabled()) {
            ItemStack offStack = mc.player.getOffHandStack();
            if (offStack.isEmpty() && lastOffHandItem != null) {
                refillOffHand(lastOffHandItem);
            } else if (!offStack.isEmpty()) {
                lastOffHandItem = offStack.getItem();
            }
        }
    }

    private void refillSlot(int hotbarSlot, Item item) {
        var inv = mc.player.getInventory();

        // Check other hotbar slots first
        for (int i = 0; i < 9; i++) {
            if (i == hotbarSlot) continue;
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty() && s.getItem() == item) {
                inv.selectedSlot = i;
                return;
            }
        }

        // Search main inventory
        for (int i = 9; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty() && s.getItem() == item) {
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        i, hotbarSlot, SlotActionType.SWAP, mc.player);
                return;
            }
        }
    }

    private void refillOffHand(Item item) {
        var inv = mc.player.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getStack(i);
            if (!s.isEmpty() && s.getItem() == item) {
                // Swap to offhand slot (slot 40 in screen handler)
                int screenSlot = i < 9 ? 36 + i : i;
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        screenSlot, 40, SlotActionType.SWAP, mc.player);
                return;
            }
        }
    }
}
