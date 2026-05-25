package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.item.ItemStack;

public class Replenish extends Module {

    private final IntSetting minCount = register(new IntSetting(
            "Min Count", "Refill hotbar slot when item count drops below this", 8, 1, 32));

    private int ticker = 0;

    public Replenish() {
        super("Replenish", "Auto-refills hotbar items from your inventory when they run low", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (++ticker < 20) return;
        ticker = 0;

        var inv = mc.player.getInventory();
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack stack = inv.getStack(hotbarSlot);
            if (stack.isEmpty() || stack.getCount() > minCount.get()) continue;

            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack candidate = inv.getStack(invSlot);
                if (candidate.isEmpty() || !candidate.isOf(stack.getItem())) continue;

                InventoryUtil.moveToHotbar(invSlot, hotbarSlot);
                return;
            }
        }
    }
}
