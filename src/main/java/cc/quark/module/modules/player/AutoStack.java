package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoStack - merges identical partial stacks together in the inventory.
 */
public class AutoStack extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between merges", 6, 1, 40));
    private int timer = 0;

    public AutoStack() {
        super("AutoStack", "Merges identical partial item stacks", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (++timer < delay.get()) return;
        timer = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack a = mc.player.getInventory().getStack(i);
            if (a.isEmpty() || a.getCount() >= a.getMaxCount()) continue;
            for (int j = i + 1; j < 36; j++) {
                ItemStack b = mc.player.getInventory().getStack(j);
                if (b.isEmpty()) continue;
                if (!a.isOf(b.getItem())) continue;
                int sa = i < 9 ? 36 + i : i;
                int sb = j < 9 ? 36 + j : j;
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                        sb, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                        sa, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                        sb, 0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }
    }
}
