package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashSet;
import java.util.Set;

public class QuickBank extends Module {

    private final BoolSetting keepHotbar = register(new BoolSetting(
            "KeepHotbar", "Do not deposit items from the hotbar", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean deposited = false;

    public QuickBank() {
        super("QuickBank", "Deposits matching items from inventory into opened chest one-click", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        deposited = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler)) {
            deposited = false;
            return;
        }
        if (deposited) return;
        if (!timer.hasReached(300)) return;
        timer.reset();

        int rows = handler.getRows();
        int chestSlotCount = rows * 9;

        Set<net.minecraft.item.Item> chestItems = new HashSet<>();
        for (int i = 0; i < chestSlotCount; i++) {
            ItemStack stack = handler.getSlot(i).getStack();
            if (!stack.isEmpty()) chestItems.add(stack.getItem());
        }

        int startSlot = keepHotbar.isEnabled() ? 9 : 0;
        int inventoryOffset = chestSlotCount;

        for (int i = startSlot; i < 36; i++) {
            int handlerSlot = inventoryOffset + i;
            if (handlerSlot >= handler.slots.size()) break;
            Slot slot = handler.getSlot(handlerSlot);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;
            if (chestItems.contains(stack.getItem())) {
                mc.interactionManager.clickSlot(handler.syncId, handlerSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }

        deposited = true;
    }
}
