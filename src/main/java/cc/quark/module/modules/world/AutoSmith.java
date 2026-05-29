package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.TimerUtil;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoSmith extends Module {

    private final TimerUtil timer = new TimerUtil();

    public AutoSmith() {
        super("AutoSmith", "Auto-uses smithing table to upgrade tools/armor when materials are available", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof SmithingScreenHandler handler)) return;
        if (!timer.hasReached(200)) return;

        // Smithing table slots: 0=template, 1=equipment, 2=material, 3=result
        // Try to take result (slot 3)
        if (handler.slots.size() > 3 && handler.slots.get(3).hasStack()) {
            mc.interactionManager.clickSlot(handler.syncId, 3, 0, SlotActionType.QUICK_MOVE, mc.player);
            timer.reset();
            return;
        }

        // Try to move inventory items into empty smithing input slots
        int slotCount = Math.min(3, handler.slots.size());
        for (int smithSlot = 0; smithSlot < slotCount; smithSlot++) {
            if (!handler.slots.get(smithSlot).hasStack()) {
                for (int invSlot = 4; invSlot < handler.slots.size(); invSlot++) {
                    if (handler.slots.get(invSlot).hasStack()) {
                        mc.interactionManager.clickSlot(handler.syncId, invSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                        timer.reset();
                        return;
                    }
                }
            }
        }
        timer.reset();
    }
}
