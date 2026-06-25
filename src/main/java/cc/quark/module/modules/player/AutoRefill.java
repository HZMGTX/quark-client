package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoRefill extends Module {

    private final IntSetting minStack = register(new IntSetting(
            "MinStack", "Refill hotbar slot when count drops below this", 5, 1, 32));

    private final TimerUtil timer = new TimerUtil();

    public AutoRefill() {
        super("AutoRefill", "Refills food, blocks, and potions from inventory when hotbar count drops low", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack hotbarStack = mc.player.getInventory().getStack(hotbarSlot);
            if (hotbarStack.isEmpty()) continue;
            if (hotbarStack.getCount() >= minStack.get()) continue;
            if (hotbarStack.getMaxCount() == 1) continue;

            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack invStack = mc.player.getInventory().getStack(invSlot);
                if (invStack.getItem() != hotbarStack.getItem()) continue;

                mc.interactionManager.clickSlot(
                        mc.player.currentScreenHandler.syncId,
                        invSlot,
                        hotbarSlot,
                        SlotActionType.SWAP,
                        mc.player);
                break;
            }
        }
    }
}
