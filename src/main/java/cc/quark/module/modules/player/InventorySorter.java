package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * InventorySorter - shift-clicks hotbar junk into the main inventory to keep the hotbar tidy.
 */
public class InventorySorter extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between moves", 4, 1, 40));
    private int timer = 0;

    public InventorySorter() {
        super("InventorySorter", "Moves loose hotbar items into the inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (++timer < delay.get()) return;
        timer = 0;

        for (int i = 0; i < 9; i++) {
            if (i == mc.player.getInventory().selectedSlot) continue;
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                    36 + i, 0, SlotActionType.QUICK_MOVE, mc.player);
            break;
        }
    }
}
