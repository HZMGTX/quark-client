package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class AutoBrew extends Module {
    private final BoolSetting collectOutput = register(new BoolSetting("Collect", "Auto-collect finished potions", true));
    private final TimerUtil timer = new TimerUtil();

    public AutoBrew() { super("AutoBrew", "Automates brewing stand operation", Category.PLAYER); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.currentScreenHandler instanceof BrewingStandScreenHandler handler)) return;
        if (!timer.hasReached(200)) return;
        // Slots 0-2 are output potions, 3 is ingredient, 4 is blaze powder
        if (collectOutput.isEnabled()) {
            for (int i = 0; i < 3; i++) {
                if (handler.getSlot(i).hasStack()) {
                    mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
            }
        }
    }
}
