package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChestSort extends Module {

    private final ModeSetting order = register(new ModeSetting(
            "Order", "Sorting order for chest contents", "Name", "Name", "Count", "Type"));

    private final TimerUtil timer = new TimerUtil();
    private boolean sorted = false;

    public ChestSort() {
        super("ChestSort", "Auto-sorts chest contents when opened", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        sorted = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler)) {
            sorted = false;
            return;
        }
        if (sorted) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        int rows = handler.getRows();
        int chestSlotCount = rows * 9;

        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < chestSlotCount; i++) {
            Slot slot = handler.getSlot(i);
            if (!slot.getStack().isEmpty()) {
                stacks.add(slot.getStack().copy());
            }
        }

        Comparator<ItemStack> comparator = switch (order.get()) {
            case "Count" -> Comparator.comparingInt(ItemStack::getCount).reversed();
            case "Type"  -> Comparator.comparing(s -> s.getItem().getClass().getSimpleName());
            default      -> Comparator.comparing(s -> s.getName().getString());
        };
        stacks.sort(comparator);

        for (int i = 0; i < chestSlotCount; i++) {
            Slot slot = handler.getSlot(i);
            if (!slot.getStack().isEmpty()) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }

        sorted = true;
    }
}
