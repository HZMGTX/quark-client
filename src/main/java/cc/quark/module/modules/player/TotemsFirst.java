package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class TotemsFirst extends Module {

    private final BoolSetting alwaysOn = register(new BoolSetting(
            "Always On", "Always maintain totem in offhand even if health is high", true));

    public TotemsFirst() {
        super("TotemsFirst", "Always keeps totem in offhand before other items", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Check if offhand already has totem
        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() == Items.TOTEM_OF_UNDYING) return;

        // Search inventory for totem
        int totemSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }

        if (totemSlot == -1) return;

        int syncId = mc.player.currentScreenHandler.syncId;

        // Swap totem to offhand slot (slot 45 in player inventory screen)
        // First pick up totem
        mc.interactionManager.clickSlot(syncId, totemSlot < 9 ? totemSlot + 36 : totemSlot,
                0, SlotActionType.PICKUP, mc.player);
        // Then place in offhand (slot 45)
        mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
        // If there was something in offhand, put it back
        ItemStack cursor = mc.player.currentScreenHandler.getCursorStack();
        if (!cursor.isEmpty()) {
            mc.interactionManager.clickSlot(syncId, totemSlot < 9 ? totemSlot + 36 : totemSlot,
                    0, SlotActionType.PICKUP, mc.player);
        }
    }
}
