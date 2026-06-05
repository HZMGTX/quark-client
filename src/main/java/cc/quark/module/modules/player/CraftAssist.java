package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.screen.CraftingScreenHandler;

public class CraftAssist extends Module {
    private final BoolSetting autoShift = register(new BoolSetting("Auto Shift", "Auto shift-click results", true));

    public CraftAssist() { super("CraftAssist", "Assists with crafting table operations", Category.PLAYER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof CraftingScreenHandler handler)) return;
        if (!autoShift.isEnabled()) return;
        if (handler.getSlot(0).hasStack()) {
            mc.interactionManager.clickSlot(handler.syncId, 0, 0, net.minecraft.screen.slot.SlotActionType.QUICK_MOVE, mc.player);
        }
    }
}
