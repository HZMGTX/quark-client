package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.screen.slot.SlotActionType;

public class AntiCurse extends Module {

    private final BoolSetting binding = register(new BoolSetting(
            "Binding", "Prevent equipping items with Curse of Binding", true));
    private final BoolSetting vanishing = register(new BoolSetting(
            "Vanishing", "Prevent equipping items with Curse of Vanishing", true));

    public AntiCurse() {
        super("AntiCurse", "Prevents cursed item equipping", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        EquipmentSlot[] armorSlots = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : armorSlots) {
            ItemStack equipped = mc.player.getEquippedStack(slot);
            if (equipped.isEmpty()) continue;
            if (!hasCurse(equipped)) continue;

            int guiSlot = getArmorGuiSlot(slot);
            if (guiSlot == -1) continue;

            int syncId = mc.player.playerScreenHandler.syncId;
            mc.interactionManager.clickSlot(syncId, guiSlot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(syncId, guiSlot, 0, SlotActionType.THROW, mc.player);
        }
    }

    private boolean hasCurse(ItemStack stack) {
        ItemEnchantmentsComponent enchants = stack.get(DataComponentTypes.ENCHANTMENTS);
        if (enchants == null) return false;

        for (RegistryEntry<Enchantment> entry : enchants.getEnchantments()) {
            String key = entry.getKey().map(k -> k.getValue().toString()).orElse("");
            if (binding.isEnabled() && key.contains("binding_curse")) return true;
            if (vanishing.isEnabled() && key.contains("vanishing_curse")) return true;
        }
        return false;
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
