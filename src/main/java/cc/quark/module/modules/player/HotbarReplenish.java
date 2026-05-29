package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class HotbarReplenish extends Module {

    private final IntSetting minCount = register(new IntSetting(
            "Min Count", "Refill hotbar slot when item count falls below this", 8, 1, 32));
    private final IntSetting delayMs = register(new IntSetting(
            "Delay ms", "Delay between refill operations", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public HotbarReplenish() {
        super("HotbarReplenish", "Auto-refills hotbar slots from inventory when item count drops", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        var inv = mc.player.getInventory();
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack hotbarStack = inv.getStack(hotbarSlot);
            if (hotbarStack.isEmpty() || hotbarStack.getCount() >= minCount.get()) continue;

            // Find matching item in main inventory (slots 9-35)
            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack candidate = inv.getStack(invSlot);
                if (candidate.isEmpty() || !candidate.isOf(hotbarStack.getItem())) continue;

                // Use SWAP action to pull from inventory to hotbar
                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        invSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
                timer.reset();
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
