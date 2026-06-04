package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.screen.slot.SlotActionType;

public class AutoSplit extends Module {

    private final IntSetting stackSize = register(new IntSetting(
            "StackSize", "Target stack size to split to", 32, 1, 64));
    private final BoolSetting onPickup = register(new BoolSetting(
            "OnPickup", "Split stacks automatically when items exceed target size", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoSplit() {
        super("AutoSplit", "Splits item stacks in inventory that exceed the target stack size", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!onPickup.isEnabled()) return;
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;
        timer.reset();

        int target = stackSize.get();
        var inv = mc.player.getInventory();
        var handler = mc.player.playerScreenHandler;

        // Scan hotbar and main inventory for over-sized stacks
        for (int i = 0; i < 36; i++) {
            var stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getCount() <= target) continue;

            // Find an empty slot to split into
            int emptySlot = -1;
            for (int j = 0; j < 36; j++) {
                if (j == i) continue;
                if (inv.getStack(j).isEmpty()) {
                    emptySlot = j;
                    break;
                }
            }
            if (emptySlot == -1) return; // No room to split

            // Convert inventory index to screen handler slot index
            // Hotbar is slots 36-44 in handler; main inv is 9-35 in handler
            int handlerSlot = i < 9 ? i + 36 : i;
            int emptyHandlerSlot = emptySlot < 9 ? emptySlot + 36 : emptySlot;

            // Right-click to pick up half, then click empty slot
            mc.interactionManager.clickSlot(handler.syncId, handlerSlot, 1, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(handler.syncId, emptyHandlerSlot, 0, SlotActionType.PICKUP, mc.player);
            // Return remainder
            mc.interactionManager.clickSlot(handler.syncId, handlerSlot, 0, SlotActionType.PICKUP, mc.player);
            return;
        }
    }
}
