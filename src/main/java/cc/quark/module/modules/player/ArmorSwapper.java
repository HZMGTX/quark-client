package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class ArmorSwapper extends Module {

    private final BoolSetting onJoin = register(new BoolSetting(
            "OnJoin", "Swap to best armor when joining a world", true));
    private final BoolSetting onDamage = register(new BoolSetting(
            "OnDamage", "Re-evaluate and swap armor when taking damage", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean joined = false;

    public ArmorSwapper() {
        super("ArmorSwapper", "Swaps to best armor from inventory", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        joined = false;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        boolean shouldCheck = false;

        if (onJoin.isEnabled() && !joined) {
            joined = true;
            shouldCheck = true;
        }

        if (onDamage.isEnabled() && mc.player.hurtTime > 0 && timer.hasReached(1000)) {
            shouldCheck = true;
            timer.reset();
        }

        if (!shouldCheck) return;

        swapBestArmor();
    }

    private void swapBestArmor() {
        EquipmentSlot[] slots = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : slots) {
            int inventoryBestSlot = findBestArmorInInventory(slot);
            if (inventoryBestSlot == -1) continue;

            ItemStack current = mc.player.getEquippedStack(slot);
            ItemStack candidate = mc.player.getInventory().getStack(inventoryBestSlot);

            if (getArmorValue(candidate) > getArmorValue(current)) {
                int syncId = mc.player.playerScreenHandler.syncId;
                int guiSlot = inventoryBestSlot < 9 ? 36 + inventoryBestSlot : inventoryBestSlot;
                // Pick up candidate
                mc.interactionManager.clickSlot(syncId, guiSlot, 0, SlotActionType.PICKUP, mc.player);
                // Place in armor slot
                int armorGuiSlot = getArmorGuiSlot(slot);
                mc.interactionManager.clickSlot(syncId, armorGuiSlot, 0, SlotActionType.PICKUP, mc.player);
                // Put old item back if cursor isn't empty
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(syncId, guiSlot, 0, SlotActionType.PICKUP, mc.player);
                }
            }
        }
    }

    private int findBestArmorInInventory(EquipmentSlot slot) {
        int bestSlot = -1;
        double bestArmor = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof ArmorItem armorItem)) continue;
            if (armorItem.getSlotType() != slot) continue;
            double val = getArmorValue(stack);
            if (val > bestArmor) {
                bestArmor = val;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private double getArmorValue(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) return 0;
        AttributeModifiersComponent comp = stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (comp == null) return 0;
        double total = 0;
        for (var entry : comp.modifiers()) {
            if (entry.attribute().value().equals(EntityAttributes.GENERIC_ARMOR)) {
                total += entry.modifier().value();
            }
            if (entry.attribute().value().equals(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)) {
                total += entry.modifier().value() * 0.5;
            }
        }
        return total;
    }

    private int getArmorGuiSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD  -> 5;
            case CHEST -> 6;
            case LEGS  -> 7;
            case FEET  -> 8;
            default    -> -1;
        };
    }
}
