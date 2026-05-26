package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoSmelt2 - when a furnace screen is open, shifts raw ores from the inventory into it.
 */
public class AutoSmelt2 extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between transfers", 5, 1, 40));
    private int timer = 0;

    public AutoSmelt2() {
        super("AutoSmelt2", "Feeds raw ores into an open furnace", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen == null) return;
        if (++timer < delay.get()) return;
        timer = 0;

        int size = mc.player.currentScreenHandler.slots.size();
        // Player inventory portion of a furnace handler is the last 36 slots.
        for (int i = size - 36; i < size; i++) {
            if (i < 0) break;
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack.isEmpty()) continue;
            if (stack.isOf(Items.RAW_IRON) || stack.isOf(Items.RAW_GOLD) || stack.isOf(Items.RAW_COPPER)) {
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                        i, 0, SlotActionType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }
}
