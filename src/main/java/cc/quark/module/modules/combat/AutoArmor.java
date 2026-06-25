package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {

    private static final int HELMET_SLOT     = 5;
    private static final int CHESTPLATE_SLOT = 6;
    private static final int LEGGINGS_SLOT   = 7;
    private static final int BOOTS_SLOT      = 8;

    private final BoolSetting preferProtection = register(new BoolSetting(
            "Prefer Protection", "Prefer Protection enchantment over pure armor value", true));

    public AutoArmor() {
        super("AutoArmor", "Automatically equips best armor from inventory", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        int syncId = mc.player.playerScreenHandler.syncId;
        int[] armorSlots = {HELMET_SLOT, CHESTPLATE_SLOT, LEGGINGS_SLOT, BOOTS_SLOT};

        for (int armorSlot : armorSlots) {
            ItemStack equipped = mc.player.playerScreenHandler.getSlot(armorSlot).getStack();
            int equippedScore = score(equipped);

            int bestSlot  = -1;
            int bestScore = equippedScore;

            for (int i = 0; i <= 35; i++) {
                ItemStack candidate = mc.player.playerScreenHandler.getSlot(i).getStack();
                if (candidate.isEmpty()) continue;
                if (!fitsSlot(candidate, armorSlot)) continue;
                int s = score(candidate);
                if (s > bestScore) {
                    bestScore = s;
                    bestSlot  = i;
                }
            }

            if (bestSlot != -1) {
                mc.interactionManager.clickSlot(syncId, bestSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }

    private int score(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) return 0;
        int base = 0;
        if (preferProtection.isEnabled()) {
            var registry = mc.world != null ? mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT) : null;
            if (registry != null) {
                var protEntry = registry.getEntry(Enchantments.PROTECTION);
                if (protEntry.isPresent()) {
                    base += EnchantmentHelper.getLevel(protEntry.get(), stack) * 2;
                }
            }
        }
        return base;
    }

    private boolean fitsSlot(ItemStack stack, int armorSlot) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return false;
        return switch (armorSlot) {
            case HELMET_SLOT     -> armor.getType() == ArmorItem.Type.HELMET;
            case CHESTPLATE_SLOT -> armor.getType() == ArmorItem.Type.CHESTPLATE;
            case LEGGINGS_SLOT   -> armor.getType() == ArmorItem.Type.LEGGINGS;
            case BOOTS_SLOT      -> armor.getType() == ArmorItem.Type.BOOTS;
            default              -> false;
        };
    }
}
