package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoAnvil extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between actions", 300, 50, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoAnvil() {
        super("AutoAnvil", "Automatically operates anvil: inputs item, material, and takes result", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof AnvilScreen)) return;
        if (!timer.hasReached(delay.get())) return;

        var handler = mc.player.currentScreenHandler;
        if (!(handler instanceof AnvilScreenHandler anvilHandler)) return;

        // Slot 0 = left item, slot 1 = right item/material, slot 2 = result
        boolean slot0Empty = !anvilHandler.slots.get(0).hasStack();
        boolean slot1Empty = !anvilHandler.slots.get(1).hasStack();
        boolean hasResult = anvilHandler.slots.get(2).hasStack();

        int containerSize = anvilHandler.slots.size();
        int invStart = 3; // Player inventory starts after slot 2

        // Take result if available
        if (hasResult) {
            mc.interactionManager.clickSlot(handler.syncId, 2, 0, SlotActionType.QUICK_MOVE, mc.player);
            timer.reset();
            return;
        }

        // Fill slot 0 from inventory if empty
        if (slot0Empty) {
            for (int i = invStart; i < containerSize; i++) {
                if (anvilHandler.slots.get(i).hasStack()) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }

        // Fill slot 1 from inventory if empty
        if (slot1Empty && !slot0Empty) {
            for (int i = invStart; i < containerSize; i++) {
                if (anvilHandler.slots.get(i).hasStack()) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }

        timer.reset();
    }
}
