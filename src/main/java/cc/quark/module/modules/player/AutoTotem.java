package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    private final DoubleSetting health = register(new DoubleSetting("HP Threshold","Keep totem in off-hand below this HP",10.0,1.0,20.0));
    public AutoTotem() { super("AutoTotem","Keeps a totem in the off-hand automatically",Category.PLAYER); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null||mc.world==null||mc.interactionManager==null) return;
        if (mc.player.getHealth() > health.get()) return;
        var offhand = mc.player.getInventory().offHand.get(0);
        if (offhand.isOf(Items.TOTEM_OF_UNDYING)) return;
        for (int i=0;i<9;i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isOf(Items.TOTEM_OF_UNDYING)) continue;
            int syncId = mc.player.currentScreenHandler.syncId;
            mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty())
                mc.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, mc.player);
            break;
        }
    }
}
