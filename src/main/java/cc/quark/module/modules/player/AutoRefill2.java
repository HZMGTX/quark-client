package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;

public class AutoRefill2 extends Module {
    private final IntSetting threshold = register(new IntSetting("Threshold", "Refill when count below", 8, 1, 64));
    private final BoolSetting refillArrows = register(new BoolSetting("RefillArrows", "Auto-refill arrows", true));
    private final BoolSetting refillFood = register(new BoolSetting("RefillFood", "Auto-refill food items", true));
    public AutoRefill2() { super("AutoRefill2", "Advanced auto-refill for hotbar items", Category.PLAYER); }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack hotbar = mc.player.getInventory().getStack(slot);
            if (hotbar.isEmpty() || hotbar.getCount() > threshold.getValue()) continue;
            // Search inventory for matching item
            for (int inv = 9; inv < 36; inv++) {
                ItemStack invStack = mc.player.getInventory().getStack(inv);
                if (!invStack.isEmpty() && invStack.getItem() == hotbar.getItem()) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                        inv, slot, net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
                    return;
                }
            }
        }
    }
}
