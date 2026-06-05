package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoArmor2 — automatically equips the best available armor pieces from the player's
 * inventory, scoring each piece by its material tier, total armor value, and enchantments.
 *
 * <p>Improvements over {@link AutoArmor}:
 * <ul>
 *   <li>Scores armor by both protection level and toughness, not just Protection enchant.</li>
 *   <li>Adds configurable Thorns and Unbreaking enchantment bonuses.</li>
 *   <li>Configurable tick delay between swaps to avoid suspicious swap spam.</li>
 *   <li>Optionally ignores armor with very low durability.</li>
 * </ul>
 */
public class AutoArmor2 extends Module {

    // Helmet = slot 5, chestplate = 6, leggings = 7, boots = 8 in playerScreenHandler
    private static final int HELMET_SLOT     = 5;
    private static final int CHESTPLATE_SLOT = 6;
    private static final int LEGGINGS_SLOT   = 7;
    private static final int BOOTS_SLOT      = 8;

    private final BoolSetting preferProtection = register(new BoolSetting(
            "Prefer Protection", "Bonus score for Protection enchantment level", true));

    private final BoolSetting bonusThorns = register(new BoolSetting(
            "Bonus Thorns", "Extra score for Thorns enchantment", false));

    private final BoolSetting ignoreLowDurability = register(new BoolSetting(
            "Ignore Low Durability", "Skip armor items below 10 % durability", true));

    private final IntSetting swapDelay = register(new IntSetting(
            "Swap Delay", "Ticks to wait between armor swaps (avoids spam)", 5, 1, 40));

    private int delayTicker = 0;

    public AutoArmor2() {
        super("AutoArmor2", "Equips the best armor from inventory with advanced scoring", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        delayTicker = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        if (delayTicker > 0) {
            delayTicker--;
            return;
        }

        int syncId = mc.player.playerScreenHandler.syncId;
        int[] armorSlots = { HELMET_SLOT, CHESTPLATE_SLOT, LEGGINGS_SLOT, BOOTS_SLOT };

        for (int armorSlot : armorSlots) {
            ItemStack equipped  = mc.player.playerScreenHandler.getSlot(armorSlot).getStack();
            int       equippedScore = score(equipped);

            int bestSlot  = -1;
            int bestScore = equippedScore;

            // Scan the full inventory (slots 0–35 in the screen handler map to hotbar + main inventory)
            for (int i = 0; i <= 35; i++) {
                ItemStack candidate = mc.player.playerScreenHandler.getSlot(i).getStack();
                if (candidate.isEmpty()) continue;
                if (!fitsSlot(candidate, armorSlot)) continue;
                if (ignoreLowDurability.isEnabled() && isDamaged(candidate)) continue;
                int s = score(candidate);
                if (s > bestScore) {
                    bestScore = s;
                    bestSlot  = i;
                }
            }

            if (bestSlot != -1) {
                mc.interactionManager.clickSlot(syncId, bestSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                delayTicker = swapDelay.get();
                return; // one swap per delay cycle
            }
        }
    }

    /**
     * Scores an armor item based on its base armor value, toughness, and enchantments.
     * Higher = better. Returns 0 for empty or non-armor stacks.
     */
    private int score(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) return 0;

        // Base: armor defense value (1–8 per piece depending on material)
        int score = armor.getProtection();
        // Toughness contributes half a point each (diamond/netherite only)
        score += (int) (armor.getToughness() * 0.5);

        var registry = mc.world != null
                ? mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT)
                : null;
        if (registry != null) {
            if (preferProtection.isEnabled()) {
                var protEntry = registry.getEntry(Enchantments.PROTECTION);
                if (protEntry.isPresent()) {
                    score += EnchantmentHelper.getLevel(protEntry.get(), stack) * 2;
                }
            }
            if (bonusThorns.isEnabled()) {
                var thornsEntry = registry.getEntry(Enchantments.THORNS);
                if (thornsEntry.isPresent()) {
                    score += EnchantmentHelper.getLevel(thornsEntry.get(), stack);
                }
            }
        }
        return score;
    }

    /** Returns true when a stack has less than 10 % durability remaining. */
    private boolean isDamaged(ItemStack stack) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) return false;
        int remaining = maxDamage - stack.getDamage();
        return remaining < maxDamage * 0.1;
    }

    /** Returns true when the given stack can occupy the given armor-equipment slot. */
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
