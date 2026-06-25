package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class Offhand extends Module {
    private final ModeSetting item = register(new ModeSetting("Item", "Item to keep in offhand", "Totem", "Totem", "Gapple", "Shield", "Crystal"));

    public Offhand() { super("Offhand", "Auto-manages your offhand item slot", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        var targetItem = switch (item.get()) {
            case "Gapple" -> Items.GOLDEN_APPLE;
            case "Shield" -> Items.SHIELD;
            case "Crystal" -> Items.END_CRYSTAL;
            default -> Items.TOTEM_OF_UNDYING;
        };
        if (mc.player.getOffHandStack().getItem() == targetItem) return;
        var handler = mc.player.currentScreenHandler;
        for (int i = 0; i < handler.slots.size(); i++) {
            if (handler.getSlot(i).getStack().getItem() == targetItem) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(handler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.getOffHandStack().isEmpty()) mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }
    }
}
