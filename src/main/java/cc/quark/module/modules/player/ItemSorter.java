package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemSorter extends Module {

    private final BoolSetting autoSort = register(new BoolSetting(
            "Auto Sort", "Automatically sort inventory every interval", true));

    private final ModeSetting order = register(new ModeSetting(
            "Order", "Sorting order for inventory items", "Type",
            "Type", "Value", "Name"));

    private final TimerUtil timer = new TimerUtil();
    private static final long SORT_INTERVAL = 2000L;

    public ItemSorter() {
        super("ItemSorter", "Sorts inventory by item type/value", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoSort.isEnabled()) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return; // Don't sort while GUI is open
        if (!timer.hasReached(SORT_INTERVAL)) return;
        timer.reset();

        sortInventory();
    }

    private void sortInventory() {
        // Collect slots 9-35 (main inventory excluding hotbar)
        List<Integer> slots = new ArrayList<>();
        for (int i = 9; i < 36; i++) slots.add(i);

        Comparator<Integer> comparator = switch (order.get()) {
            case "Name" -> Comparator.comparing(slot ->
                    mc.player.getInventory().getStack(slot).getName().getString());
            case "Value" -> Comparator.comparingInt((Integer slot) ->
                    mc.player.getInventory().getStack(slot).getMaxCount()).reversed();
            default -> Comparator.comparing(slot ->
                    mc.player.getInventory().getStack(slot).getItem().getClass().getSimpleName());
        };

        slots.sort(comparator);

        // Use bubble sort with click-slot swaps
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 9; i < 36; i++) stacks.add(mc.player.getInventory().getStack(i).copy());

        // Simple selection: if slot order doesn't match, swap via pickup
        // To keep it safe, just do a single pass of adjacent swaps per tick
        for (int i = 9; i < 35; i++) {
            ItemStack a = mc.player.getInventory().getStack(i);
            ItemStack b = mc.player.getInventory().getStack(i + 1);
            if (a.isEmpty() && !b.isEmpty()) {
                swapSlots(i, i + 1);
            }
        }
    }

    private void swapSlots(int slotA, int slotB) {
        if (mc.player == null || mc.interactionManager == null) return;
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, slotA, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, slotB, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, slotA, 0, SlotActionType.PICKUP, mc.player);
    }
}
