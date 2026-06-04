package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;

public class FlipFlop extends Module {

    private final IntSetting slot1 = register(new IntSetting("Slot 1", "First hotbar slot (0-8)", 0, 0, 8));
    private final IntSetting slot2 = register(new IntSetting("Slot 2", "Second hotbar slot (0-8)", 1, 0, 8));
    private final IntSetting switchMs = register(new IntSetting("Switch Ms", "Milliseconds between slot switches", 200, 50, 1000));

    private final TimerUtil timer = new TimerUtil();
    private boolean onSlot1 = true;

    public FlipFlop() {
        super("FlipFlop", "Alternates between two weapon slots for fast attacks", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        onSlot1 = true;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.getInventory().selectedSlot = slot1.get();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached(switchMs.get())) return;

        int targetSlot = onSlot1 ? slot1.get() : slot2.get();
        mc.player.getInventory().selectedSlot = Math.max(0, Math.min(8, targetSlot));
        onSlot1 = !onSlot1;
        timer.reset();
    }
}
