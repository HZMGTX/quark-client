package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem2 extends Module {
    private final IntSetting hpThreshold = register(new IntSetting("HP Threshold", "HP to switch totem", 6, 1, 20));
    private final BoolSetting alwaysEquip = register(new BoolSetting("Always Equip", "Keep totem in offhand always", true));

    public AutoTotem2() { super("AutoTotem2", "Enhanced auto-totem with smart HP threshold", Category.COMBAT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        boolean needTotem = alwaysEquip.isEnabled() || mc.player.getHealth() <= hpThreshold.get();
        if (!needTotem) return;
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
        var handler = mc.player.currentScreenHandler;
        for (int i = 0; i < handler.slots.size(); i++) {
            if (handler.getSlot(i).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(handler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.getOffHandStack().isEmpty()) mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.PICKUP, mc.player);
                return;
            }
        }
    }
}
