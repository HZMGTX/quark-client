package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.*;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class StorageOrganizer extends Module {

    private final IntSetting delayMs = register(new IntSetting(
            "Delay ms", "Milliseconds between sort operations", 300, 50, 2000));
    private final BoolSetting stackFirst = register(new BoolSetting(
            "Stack First", "Merge partial stacks before sorting by type", true));

    private final TimerUtil timer = new TimerUtil();
    private int sortPass = 0;

    public StorageOrganizer() {
        super("StorageOrganizer", "Auto-sorts open chest/container contents by item type and value", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        sortPass = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler)) return;
        if (!timer.hasReached(delayMs.get())) return;
        timer.reset();

        int containerSize = handler.getRows() * 9;
        int syncId = handler.syncId;

        if (stackFirst.isEnabled() && tryStack(handler, containerSize, syncId)) return;

        trySort(handler, containerSize, syncId);
    }

    private boolean tryStack(GenericContainerScreenHandler handler, int size, int syncId) {
        for (int i = 0; i < size; i++) {
            ItemStack a = handler.getSlot(i).getStack();
            if (a.isEmpty() || a.getCount() >= a.getMaxCount()) continue;

            for (int j = i + 1; j < size; j++) {
                ItemStack b = handler.getSlot(j).getStack();
                if (b.isEmpty() || !ItemStack.areItemsEqual(a, b)) continue;

                mc.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, mc.player);
                return true;
            }
        }
        return false;
    }

    private void trySort(GenericContainerScreenHandler handler, int size, int syncId) {
        for (int i = 0; i < size - 1; i++) {
            ItemStack a = handler.getSlot(i).getStack();
            ItemStack b = handler.getSlot(i + 1).getStack();
            if (a.isEmpty() || b.isEmpty()) continue;

            if (slotValue(a) < slotValue(b)) {
                mc.interactionManager.clickSlot(syncId, i,     0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, i + 1, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(syncId, i,     0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }
    }

    private int slotValue(ItemStack stack) {
        if (stack.getItem() instanceof SwordItem)   return 1000;
        if (stack.getItem() instanceof PickaxeItem) return 900;
        if (stack.getItem() instanceof AxeItem)     return 850;
        if (stack.getItem() instanceof ShovelItem)  return 800;
        if (stack.getItem() instanceof ArmorItem a) return 700 + a.getProtection();
        if (stack.getItem() instanceof BowItem)     return 600;
        if (stack.getItem() instanceof CrossbowItem) return 590;
        //? if mc >= "1.20.5" {
        if (stack.contains(net.minecraft.component.DataComponentTypes.FOOD)) return 400;
        //?} else {
        /*if (stack.getItem().isFood()) return 400;*/
        //?}
        return stack.getCount();
    }
}
