package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoArmorEquip - equips the best available armor from inventory using
 * priority-based scoring (Protection or Toughness).
 */
public class AutoArmorEquip extends Module {

    private static final int HELMET_SLOT     = 5;
    private static final int CHESTPLATE_SLOT = 6;
    private static final int LEGGINGS_SLOT   = 7;
    private static final int BOOTS_SLOT      = 8;

    private final ModeSetting priority = register(new ModeSetting(
            "Priority", "Armor comparison strategy", "Protection", "Protection", "Toughness"));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between equip actions", 200, 0, 500));

    private final TimerUtil timer = new TimerUtil();

    public AutoArmorEquip() {
        super("AutoArmorEquip", "Automatically equips best armor from inventory", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!timer.hasReached(delay.get())) return;

        int syncId = mc.player.playerScreenHandler.syncId;
        int[] armorSlots = {HELMET_SLOT, CHESTPLATE_SLOT, LEGGINGS_SLOT, BOOTS_SLOT};

        for (int armorSlot : armorSlots) {
            ItemStack equipped = mc.player.playerScreenHandler.getSlot(armorSlot).getStack();
            int equippedScore = score(equipped);

            int bestSlot  = -1;
            int bestScore = equippedScore;

            // Scan all inventory slots (0-35 in the playerScreenHandler correspond to
            // the 36 hotbar+main inventory slots starting at index 9 for main inv)
            for (int i = 9; i <= 44; i++) {
                // Skip the armor slots themselves
                if (i >= HELMET_SLOT && i <= BOOTS_SLOT) continue;
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
                timer.reset();
                return;
            }
        }
    }

    private int score(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) return 0;

        if (priority.is("Toughness")) {
            return (int)(armor.getMaterial().value().toughness() * 10);
        }

        // Protection priority: base defense + enchantment bonus
        int base = armor.getProtection();
        if (mc.world != null) {
            var registry = mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT);
            var protEntry = registry.getEntry(Enchantments.PROTECTION);
            if (protEntry.isPresent()) {
                base += EnchantmentHelper.getLevel(protEntry.get(), stack) * 2;
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
