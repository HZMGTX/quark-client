package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class ChestStealer extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between steals", 50, 0, 500));

    private long lastSteal = 0;

    public ChestStealer() {
        super("ChestStealer", "Automatically steals all items from opened chests", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof GenericContainerScreen)) return;
        if (!(mc.player.currentScreenHandler instanceof GenericContainerScreenHandler handler)) return;

        long now = System.currentTimeMillis();
        if (now - lastSteal < delay.get()) return;

        List<Slot> slots = handler.slots;
        int containerSize = handler.getRows() * 9;

        for (int i = 0; i < containerSize; i++) {
            Slot slot = slots.get(i);
            if (!slot.hasStack()) continue;

            mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            lastSteal = now;
            return;
        }
    }
}
