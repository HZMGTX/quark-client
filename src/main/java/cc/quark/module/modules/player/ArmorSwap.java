package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class ArmorSwap extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Durability % below which armor is replaced", 20, 1, 100));

    private final TimerUtil timer = new TimerUtil();

    public ArmorSwap() {
        super("ArmorSwap", "Auto-equips better armor from inventory when current armor is damaged", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(2000)) return;
        timer.reset();

        for (int armorSlot = 0; armorSlot < 4; armorSlot++) {
            ItemStack worn = mc.player.getInventory().getArmorStack(armorSlot);
            if (worn.isEmpty()) continue;
            if (!(worn.getItem() instanceof ArmorItem)) continue;

            int maxDurability = worn.getMaxDamage();
            if (maxDurability <= 0) continue;

            int durabilityPct = (int) (((float) (maxDurability - worn.getDamage()) / maxDurability) * 100);
            if (durabilityPct > threshold.get()) continue;

            int betterSlot = findBetterArmor(armorSlot, (ArmorItem) worn.getItem(), durabilityPct);
            if (betterSlot == -1) continue;

            int containerArmorSlot = 8 - armorSlot;
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    betterSlot,
                    containerArmorSlot,
                    SlotActionType.SWAP,
                    mc.player);
        }
    }

    private int findBetterArmor(int armorSlot, ArmorItem currentItem, int currentDurabilityPct) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ArmorItem candidate)) continue;
            if (candidate.getType() != currentItem.getType()) continue;

            int maxDurability = stack.getMaxDamage();
            if (maxDurability <= 0) continue;

            int candidatePct = (int) (((float) (maxDurability - stack.getDamage()) / maxDurability) * 100);
            if (candidatePct > currentDurabilityPct) {
                return i;
            }
        }
        return -1;
    }
}
