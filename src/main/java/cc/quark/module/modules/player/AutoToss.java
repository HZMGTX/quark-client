package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.screen.slot.SlotActionType;

public class AutoToss extends Module {

    private final IntSetting count = register(new IntSetting(
            "Count", "Number of items to toss per action", 1, 1, 64));
    private final BoolSetting stack = register(new BoolSetting(
            "Stack", "Throw entire stack at once", false));

    private final TimerUtil timer = new TimerUtil();

    public AutoToss() {
        super("AutoToss", "Throws held items toward cursor direction", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;
        timer.reset();

        int selectedSlot = mc.player.getInventory().selectedSlot;
        if (mc.player.getInventory().getStack(selectedSlot).isEmpty()) return;

        int syncId = mc.player.playerScreenHandler.syncId;
        int guiSlot = 36 + selectedSlot;

        if (stack.isEnabled()) {
            // Throw full stack
            mc.interactionManager.clickSlot(syncId, guiSlot, 1, SlotActionType.THROW, mc.player);
        } else {
            // Throw one item at a time, 'count' times
            int thrown = 0;
            while (thrown < count.get() && !mc.player.getInventory().getStack(selectedSlot).isEmpty()) {
                mc.interactionManager.clickSlot(syncId, guiSlot, 0, SlotActionType.THROW, mc.player);
                thrown++;
            }
        }
    }
}
