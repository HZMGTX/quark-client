package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

public class InventoryFill extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Max distance from chest to pull items from", 4.0, 1.0, 8.0));

    private final TimerUtil timer = new TimerUtil();

    public InventoryFill() {
        super("InventoryFill", "Fills empty inventory slots from nearby chests", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(1000)) return;
        timer.reset();

        // Count empty inventory slots
        int emptySlots = 0;
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) emptySlots++;
        }
        if (emptySlots == 0) return;

        // Check if a chest screen is open
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;

        // Take items from container screen (slot 0 to size-9, last 27 are player inv)
        int containerSize = screen.getScreenHandler().slots.size() - 36;
        int syncId = screen.getScreenHandler().syncId;

        for (int i = 0; i < containerSize && emptySlots > 0; i++) {
            ItemStack stack = screen.getScreenHandler().getSlot(i).getStack();
            if (stack.isEmpty()) continue;

            // Shift-click to move item to player inventory
            mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            emptySlots--;
        }
    }
}
