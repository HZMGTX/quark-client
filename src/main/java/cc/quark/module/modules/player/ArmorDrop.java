package cc.quark.module.modules.player;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class ArmorDrop extends Module {

    private final BoolSetting confirm = register(new BoolSetting(
            "Confirm", "Must be enabled to actually drop armor", false));

    public ArmorDrop() {
        super("ArmorDrop", "Drops all equipped armor when enabled (Confirm must be on)", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.interactionManager == null) {
            disable();
            return;
        }

        if (!confirm.isEnabled()) {
            ChatUtil.warn("[ArmorDrop] Enable 'Confirm' setting first to drop armor.");
            disable();
            return;
        }

        int syncId = mc.player.playerScreenHandler.syncId;
        int dropped = 0;

        // Armor screen handler slots: 5=helmet, 6=chestplate, 7=leggings, 8=boots
        int[] armorScreenSlots = {5, 6, 7, 8};
        for (int armorSlot : armorScreenSlots) {
            ItemStack stack = mc.player.playerScreenHandler.getSlot(armorSlot).getStack();
            if (!stack.isEmpty()) {
                // Use Ctrl+Q (drop entire stack) equivalent via THROW with button=1
                mc.interactionManager.clickSlot(syncId, armorSlot, 1, SlotActionType.THROW, mc.player);
                dropped++;
            }
        }

        if (dropped > 0) {
            ChatUtil.success("[ArmorDrop] Dropped " + dropped + " armor piece(s).");
        } else {
            ChatUtil.info("[ArmorDrop] No armor to drop.");
        }

        disable();
    }
}
