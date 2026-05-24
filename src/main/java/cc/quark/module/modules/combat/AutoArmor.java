package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/**
 * AutoArmor - automatically equips the best available armor from the player's
 * inventory, replacing currently-worn pieces only when a strictly better piece
 * is available (if onlyBetter is enabled).
 *
 * <p>PlayerScreenHandler slot layout for player inventory:
 * <pre>
 *   Slot  5 = helmet
 *   Slot  6 = chestplate
 *   Slot  7 = leggings
 *   Slot  8 = boots
 *   Slots 9-35  = main inventory
 *   Slots 0-8   = hotbar
 * </pre>
 */
public class AutoArmor extends Module {

    private static final int HELMET_SLOT     = 5;
    private static final int CHESTPLATE_SLOT = 6;
    private static final int LEGGINGS_SLOT   = 7;
    private static final int BOOTS_SLOT      = 8;

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between equip actions to avoid server spam", 2, 0, 10));

    private final BoolSetting onlyBetter = register(new BoolSetting(
            "Only Better", "Only equip armor that is strictly better than what is worn", true));

    /** Countdown in ticks before the next equip action is allowed. */
    private int cooldown = 0;

    public AutoArmor() {
        super("AutoArmor", "Automatically equips the best armor from inventory", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        cooldown = 0;
    }

    @Override
    public void onDisable() {}

    @EventHandler
    public void onTick(EventTick event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.interactionManager == null) return;
        // Do not run while a container screen (chest, crafting, etc.) is open
        if (mc.currentScreen != null) return;

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (tryEquipBest(mc)) {
            cooldown = Math.max(1, delay.get());
        }
    }

    /**
     * Iterates over all four armor slots and shift-clicks the best available
     * piece from the main inventory / hotbar into the appropriate slot.
     *
     * @return true if any equip action was performed
     */
    private boolean tryEquipBest(MinecraftClient mc) {
        int syncId = mc.player.playerScreenHandler.syncId;
        int[] armorSlots = {HELMET_SLOT, CHESTPLATE_SLOT, LEGGINGS_SLOT, BOOTS_SLOT};

        for (int armorSlot : armorSlots) {
            ItemStack equipped    = mc.player.playerScreenHandler.getSlot(armorSlot).getStack();
            int equippedProtection = getProtection(equipped);

            int bestSlot       = -1;
            int bestProtection = onlyBetter.isEnabled() ? equippedProtection : -1;

            // Scan hotbar (0-8) and main inventory (9-35)
            for (int i = 0; i <= 35; i++) {
                ItemStack candidate = mc.player.playerScreenHandler.getSlot(i).getStack();
                if (candidate.isEmpty()) continue;
                if (!fitsArmorSlot(candidate, armorSlot)) continue;

                int prot = getProtection(candidate);
                if (prot > bestProtection) {
                    bestProtection = prot;
                    bestSlot       = i;
                }
            }

            if (bestSlot != -1) {
                // Shift-click sends the item directly to the appropriate armor slot
                mc.interactionManager.clickSlot(syncId, bestSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                return true; // One piece per tick to avoid confusion
            }
        }
        return false;
    }

    /**
     * Returns the base armor protection value for the given item stack (0 if not armor).
     * Uses the ArmorItem's material protection to score pieces.
     */
    private int getProtection(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getProtection();
        }
        return 0;
    }

    /**
     * Returns {@code true} when the item in {@code stack} fits the given armor container slot.
     */
    private boolean fitsArmorSlot(ItemStack stack, int armorSlot) {
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
