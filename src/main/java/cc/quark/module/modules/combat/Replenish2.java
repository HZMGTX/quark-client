package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class Replenish2 extends Module {

    private final BoolSetting onlyOutOfCombat = register(new BoolSetting("OnlyOutOfCombat", "Only refill when not recently attacked", true));
    private final TimerUtil combatTimer = new TimerUtil();
    private final TimerUtil refillTimer = new TimerUtil();

    public Replenish2() {
        super("Replenish2", "Refills hotbar items from inventory during combat pause", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        combatTimer.reset();
        refillTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!refillTimer.hasReached(1000)) return;
        if (onlyOutOfCombat.isEnabled() && !combatTimer.hasReached(4000)) return;
        if (mc.player.currentScreenHandler == null) return;

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            ItemStack hotbarStack = mc.player.getInventory().getStack(hotbarSlot);
            if (hotbarStack.isEmpty()) continue;
            if (!hotbarStack.isDamageable() && hotbarStack.getCount() > 1) continue;
            if (hotbarStack.isDamageable() && hotbarStack.getDamage() < hotbarStack.getMaxDamage() / 2) continue;

            Item targetItem = hotbarStack.getItem();
            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack invStack = mc.player.getInventory().getStack(invSlot);
                if (invStack.getItem() == targetItem && invStack.getCount() > 0) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId,
                            invSlot, hotbarSlot, SlotActionType.SWAP, mc.player);
                    refillTimer.reset();
                    return;
                }
            }
        }
    }
}
