package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.InventoryUtil;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class XCarry extends Module {

    private final ModeSetting fillItem = register(new ModeSetting(
            "Fill Item", "Item to keep in offhand",
            "Totem", "Totem", "Shield", "Pearl", "Food", "None"));

    public XCarry() {
        super("XCarry", "Cancels inventory-close packets and auto-fills offhand with configured item", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        // Cancel inventory close to carry extra items
        if (event.getPacket() instanceof CloseHandledScreenC2SPacket) {
            event.cancel();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (fillItem.is("None")) return;

        // Only fill when offhand is empty
        if (!mc.player.getOffHandStack().isEmpty()) return;

        net.minecraft.item.Item target = switch (fillItem.get()) {
            case "Totem"  -> Items.TOTEM_OF_UNDYING;
            case "Shield" -> Items.SHIELD;
            case "Pearl"  -> Items.ENDER_PEARL;
            case "Food"   -> null; // handled below
            default       -> null;
        };

        if (fillItem.is("Food")) {
            // Find any food item in inventory
            for (int i = 9; i < 36; i++) {
                var stack = mc.player.getInventory().getStack(i);
                if (stack.isFood()) {
                    // Swap to offhand slot (slot 40 in screen handler)
                    mc.interactionManager.clickSlot(
                            mc.player.currentScreenHandler.syncId,
                            i, 40, SlotActionType.SWAP, mc.player);
                    return;
                }
            }
            return;
        }

        if (target == null) return;

        int slot = InventoryUtil.findItem(target);
        if (slot < 0) return;

        // Convert inventory slot to screen slot index
        int screenSlot = slot < 9 ? 36 + slot : slot;
        // Swap to offhand (slot 40 in player inventory screen handler)
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                screenSlot, 40, SlotActionType.SWAP, mc.player);
    }
}
