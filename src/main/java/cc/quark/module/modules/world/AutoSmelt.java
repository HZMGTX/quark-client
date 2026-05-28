package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoSmelt extends Module {

    private final TimerUtil timer = new TimerUtil();

    public AutoSmelt() {
        super("AutoSmelt", "When a furnace is open, auto-fills fuel and smeltable items from inventory", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen == null) return;
        if (!timer.hasReached(200)) return;
        timer.reset();

        var handler = mc.player.currentScreenHandler;
        if (!(handler instanceof AbstractFurnaceScreenHandler furnace)) return;

        int totalSlots = furnace.slots.size();
        int playerStart = totalSlots - 36;

        boolean needsFuel = !furnace.slots.get(1).hasStack();
        boolean needsInput = !furnace.slots.get(0).hasStack();

        if (!needsFuel && !needsInput) return;

        for (int i = playerStart; i < totalSlots; i++) {
            var stack = furnace.slots.get(i).getStack();
            if (stack.isEmpty()) continue;

            Item item = stack.getItem();

            if (needsFuel && isFuel(item)) {
                mc.interactionManager.clickSlot(furnace.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                return;
            }

            if (needsInput && isSmeltable(item)) {
                mc.interactionManager.clickSlot(furnace.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }

    private boolean isFuel(Item item) {
        return item == Items.COAL || item == Items.CHARCOAL || item == Items.COAL_BLOCK
                || item == Items.OAK_LOG || item == Items.SPRUCE_LOG || item == Items.BIRCH_LOG
                || item == Items.JUNGLE_LOG || item == Items.ACACIA_LOG || item == Items.DARK_OAK_LOG
                || item == Items.OAK_PLANKS || item == Items.SPRUCE_PLANKS || item == Items.BIRCH_PLANKS;
    }

    private boolean isSmeltable(Item item) {
        return item == Items.RAW_IRON || item == Items.RAW_GOLD || item == Items.RAW_COPPER
                || item == Items.IRON_ORE || item == Items.GOLD_ORE
                || item == Items.DEEPSLATE_IRON_ORE || item == Items.DEEPSLATE_GOLD_ORE
                || item == Items.SAND || item == Items.COBBLESTONE
                || item == Items.STONE || item == Items.NETHERRACK;
    }
}
