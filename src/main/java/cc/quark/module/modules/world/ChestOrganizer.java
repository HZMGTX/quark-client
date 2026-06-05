package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

/**
 * ChestOrganizer - Sorts chests by pushing stacks from your inventory into
 * the chest when a matching item already exists there (stack consolidation),
 * and optionally sorts the chest contents alphabetically.
 */
public class ChestOrganizer extends Module {

    private final BoolSetting consolidate = register(new BoolSetting(
            "Consolidate", "Push inventory items into chest slots that already hold the same item", true));
    private final BoolSetting sort = register(new BoolSetting(
            "Sort", "Sort chest contents alphabetically after consolidation", false));
    private final BoolSetting onOpen = register(new BoolSetting(
            "OnOpen", "Trigger automatically when a chest is opened", true));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between slot actions", 50, 10, 500));

    private final TimerUtil timer = new TimerUtil();
    private boolean organized = false;
    private int consolidateStep = 0;

    public ChestOrganizer() {
        super("ChestOrganizer", "Sorts chests by pushing matching stacks in and optionally sorting contents", Category.WORLD);
    }

    @Override
    public void onEnable() {
        organized = false;
        consolidateStep = 0;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        var handler = mc.player.currentScreenHandler;
        if (!(handler instanceof GenericContainerScreenHandler chest)) {
            // Reset when chest closes
            organized = false;
            consolidateStep = 0;
            return;
        }

        if (!onOpen.isEnabled() && organized) return;
        if (!timer.hasReached(delay.get())) return;

        int rows = chest.getRows();
        int chestSize = rows * 9;
        int totalSlots = chest.slots.size();
        int playerInvStart = chestSize; // player inventory follows chest slots

        // Phase 1: Consolidate — push matching items from player inventory into chest
        if (consolidate.isEnabled() && consolidateStep < chestSize) {
            Slot chestSlot = chest.slots.get(consolidateStep);
            if (chestSlot.hasStack()) {
                Item chestItem = chestSlot.getStack().getItem();
                // Find same item in player inventory and shift-click it
                for (int pi = playerInvStart; pi < totalSlots; pi++) {
                    ItemStack playerStack = chest.slots.get(pi).getStack();
                    if (!playerStack.isEmpty() && playerStack.getItem() == chestItem) {
                        mc.interactionManager.clickSlot(chest.syncId, pi, 0, SlotActionType.QUICK_MOVE, mc.player);
                        timer.reset();
                        consolidateStep++;
                        return;
                    }
                }
            }
            consolidateStep++;
            return;
        }

        // Phase 2: Sort chest alphabetically
        if (sort.isEnabled() && !organized) {
            sortChestAlpha(chest, chestSize);
            organized = true;
            timer.reset();
            return;
        }

        organized = true;
    }

    private void sortChestAlpha(GenericContainerScreenHandler chest, int chestSize) {
        // Build list of occupied chest slot indices sorted by item name
        List<Integer> occupied = new ArrayList<>();
        for (int i = 0; i < chestSize; i++) {
            if (chest.slots.get(i).hasStack()) occupied.add(i);
        }
        occupied.sort((a, b) -> chest.slots.get(a).getStack().getName().getString()
                .compareToIgnoreCase(chest.slots.get(b).getStack().getName().getString()));

        // Shift-click each to player inventory in sorted order, then shift-click back
        for (int idx : occupied) {
            mc.interactionManager.clickSlot(chest.syncId, idx, 0, SlotActionType.QUICK_MOVE, mc.player);
        }
        // Items moved to player inventory; shift-clicking them back inserts in first available slot
        int totalSlots = chest.slots.size();
        int playerInvStart = chestSize;
        for (int pi = playerInvStart; pi < totalSlots; pi++) {
            if (chest.slots.get(pi).hasStack()) {
                mc.interactionManager.clickSlot(chest.syncId, pi, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }
    }
}
