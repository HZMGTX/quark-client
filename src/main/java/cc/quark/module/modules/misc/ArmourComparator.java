package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class ArmourComparator extends Module {

    private final BoolSetting autoEquip = register(new BoolSetting(
            "AutoEquip", "Automatically equip better armor found in inventory", false));

    // Armor slots in playerScreenHandler: 5=helmet, 6=chestplate, 7=leggings, 8=boots
    private static final int[] ARMOR_SCREEN_SLOTS = {5, 6, 7, 8};

    private int tickCounter = 0;

    public ArmourComparator() {
        super("ArmourComparator", "Alerts when better armor is found in inventory", Category.MISC);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        tickCounter++;
        if (tickCounter < 40) return;
        tickCounter = 0;

        int syncId = mc.player.playerScreenHandler.syncId;

        for (int armorSlotIdx = 0; armorSlotIdx < ARMOR_SCREEN_SLOTS.length; armorSlotIdx++) {
            int armorSlot = ARMOR_SCREEN_SLOTS[armorSlotIdx];
            ItemStack equipped = mc.player.playerScreenHandler.getSlot(armorSlot).getStack();
            int equippedScore = getArmorScore(equipped);

            int bestSlot = -1;
            int bestScore = equippedScore;

            // Search inventory (slots 9-44 in playerScreenHandler = slots 0-35 in inventory)
            for (int i = 9; i <= 44; i++) {
                ItemStack candidate = mc.player.playerScreenHandler.getSlot(i).getStack();
                if (candidate.isEmpty()) continue;
                if (!fitsArmorSlot(candidate, armorSlot)) continue;
                int score = getArmorScore(candidate);
                if (score > bestScore) {
                    bestScore = score;
                    bestSlot = i;
                }
            }

            if (bestSlot != -1) {
                String slotName = getSlotName(armorSlot);
                NotificationOverlay.send("Armour", "Better item in slot " + slotName, NotificationOverlay.NotifType.INFO);

                if (autoEquip.isEnabled()) {
                    mc.interactionManager.clickSlot(syncId, bestSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
                // Only report one improvement per check cycle
                break;
            }
        }
    }

    private int getArmorScore(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem armor)) return 0;
        return 0;
    }

    private boolean fitsArmorSlot(ItemStack stack, int armorSlot) {
        if (!(stack.getItem() instanceof ArmorItem armor)) return false;
        return switch (armorSlot) {
            case 5 -> armor.getType() == ArmorItem.Type.HELMET;
            case 6 -> armor.getType() == ArmorItem.Type.CHESTPLATE;
            case 7 -> armor.getType() == ArmorItem.Type.LEGGINGS;
            case 8 -> armor.getType() == ArmorItem.Type.BOOTS;
            default -> false;
        };
    }

    private String getSlotName(int armorSlot) {
        return switch (armorSlot) {
            case 5 -> "Helmet";
            case 6 -> "Chestplate";
            case 7 -> "Leggings";
            case 8 -> "Boots";
            default -> "Unknown";
        };
    }
}
