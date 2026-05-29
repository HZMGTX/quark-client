package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoSmelt2 extends Module {

    private final IntSetting delayMs = register(new IntSetting(
            "Delay ms", "Milliseconds between item transfers", 200, 50, 2000));
    private final BoolSetting addFuel = register(new BoolSetting(
            "Add Fuel", "Also add coal/charcoal as fuel if slot is empty", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoSmelt2() {
        super("AutoSmelt2", "Feeds raw ores and fuel into an open furnace screen", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof AbstractFurnaceScreenHandler furnace)) return;
        if (!timer.hasReached(delayMs.get())) return;
        timer.reset();

        int syncId = furnace.syncId;

        // Fuel slot (slot 1) – fill if empty and addFuel is on
        if (addFuel.isEnabled() && furnace.getSlot(1).getStack().isEmpty()) {
            int fuelSlot = findFuel();
            if (fuelSlot >= 0) {
                mc.interactionManager.clickSlot(syncId, fuelSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                return;
            }
        }

        // Input slot (slot 0) – fill with smeltable items
        if (furnace.getSlot(0).getStack().getCount() < 64) {
            int size = furnace.slots.size();
            for (int i = 3; i < size; i++) {  // player inventory starts at slot 3 in furnace
                ItemStack stack = furnace.getSlot(i).getStack();
                if (isSmeltable(stack)) {
                    mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    return;
                }
            }
        }
    }

    private boolean isSmeltable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.isOf(Items.RAW_IRON) || stack.isOf(Items.RAW_GOLD) || stack.isOf(Items.RAW_COPPER)
            || stack.isOf(Items.IRON_ORE) || stack.isOf(Items.GOLD_ORE) || stack.isOf(Items.COPPER_ORE)
            || stack.isOf(Items.SAND)     || stack.isOf(Items.COBBLESTONE) || stack.isOf(Items.GRAVEL);
    }

    private int findFuel() {
        if (mc.player == null) return -1;
        var handler = mc.player.currentScreenHandler;
        for (int i = 3; i < handler.slots.size(); i++) {
            ItemStack s = handler.getSlot(i).getStack();
            if (s.isOf(Items.COAL) || s.isOf(Items.CHARCOAL) || s.isOf(Items.COAL_BLOCK)) return i;
        }
        return -1;
    }
}
