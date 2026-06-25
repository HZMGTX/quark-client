package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.gui.screen.ingame.SmithingScreen;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class SmithingHelper extends Module {

    private final BoolSetting autoUpgrade = register(new BoolSetting(
            "Auto Upgrade", "Automatically click upgrade button when smithing table is ready", true));

    private final TimerUtil timer = new TimerUtil();
    private static final long ACTION_DELAY = 500L;

    public SmithingHelper() {
        super("SmithingHelper", "Assists with smithing table upgrades", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoUpgrade.isEnabled()) return;
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.currentScreen instanceof SmithingScreen)) return;
        if (!timer.hasReached(ACTION_DELAY)) return;

        SmithingScreenHandler handler = (SmithingScreenHandler) mc.player.currentScreenHandler;

        // Smithing output slot index is 3 (template=0, base=1, addition=2, output=3)
        int outputSlot = 3;
        if (handler.slots.size() <= outputSlot) return;

        if (handler.getSlot(outputSlot).getStack().isEmpty()) return;

        // Shift-click to take the result
        mc.interactionManager.clickSlot(
                handler.syncId,
                outputSlot,
                0,
                SlotActionType.QUICK_MOVE,
                mc.player);

        timer.reset();
    }
}
