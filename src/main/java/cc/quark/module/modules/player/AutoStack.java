package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoStack extends Module {

    private final IntSetting delay = register(new IntSetting("Delay ms", "Milliseconds between stack operations", 300, 100, 1000));
    private final TimerUtil timer = new TimerUtil();

    public AutoStack() {
        super("AutoStack", "Auto-stacks identical items in inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!timer.hasReached(delay.get())) return;
        timer.reset();

        for (int i = 0; i < 36; i++) {
            ItemStack a = mc.player.getInventory().getStack(i);
            if (a.isEmpty() || a.getCount() >= a.getMaxCount()) continue;
            for (int j = i + 1; j < 36; j++) {
                ItemStack b = mc.player.getInventory().getStack(j);
                if (b.isEmpty() || !a.isOf(b.getItem())) continue;
                int slotA = i < 9 ? 36 + i : i;
                int slotB = j < 9 ? 36 + j : j;
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotB, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotA, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotB, 0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }
    }
}
