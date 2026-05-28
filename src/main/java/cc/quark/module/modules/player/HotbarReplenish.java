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

    private final IntSetting minCount = register(new IntSetting("Min Count", "Refill hotbar slot when count drops below this", 8, 1, 32));
    private final TimerUtil timer = new TimerUtil();

    public HotbarReplenish() {
        super("HotbarReplenish", "Auto-refills hotbar slots from inventory when item count drops", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack hotbarStack = mc.player.getInventory().getStack(hotbarSlot);
            if (hotbarStack.isEmpty() || hotbarStack.getCount() >= minCount.get()) continue;

            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack invStack = mc.player.getInventory().getStack(invSlot);
                if (!invStack.isEmpty() && invStack.isOf(hotbarStack.getItem())) {
                    mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        invSlot, hotbarSlot, SlotActionType.SWAP, mc.player
                    );
                    return;
                }
            }
        }
    }
}
