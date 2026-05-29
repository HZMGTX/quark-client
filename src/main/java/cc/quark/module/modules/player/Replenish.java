package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.InventoryUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;

public class Replenish extends Module {

    private final IntSetting minCount = register(new IntSetting(
            "Min Count", "Refill hotbar slot when item count drops below this", 8, 1, 32));
    private final IntSetting delayMs = register(new IntSetting(
            "Delay ms", "Milliseconds between refill checks", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public Replenish() {
        super("Replenish", "Auto-refills hotbar items from inventory when they run low", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;
        timer.reset();

        var inv = mc.player.getInventory();
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack stack = inv.getStack(hotbarSlot);
            if (stack.isEmpty() || stack.getCount() > minCount.get()) continue;

            // Find matching item in main inventory (slots 9-35)
            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack candidate = inv.getStack(invSlot);
                if (candidate.isEmpty() || !candidate.isOf(stack.getItem())) continue;

                InventoryUtil.moveToHotbar(invSlot, hotbarSlot);
                return;
            }
        }
    }

    @Override
    public String getSuffix() {
        if (mc.player == null) return "";
        int low = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!s.isEmpty() && s.getCount() < minCount.get()) low++;
        }
        return low > 0 ? low + " low" : "";
    }
}
