package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;

public class SwapBack extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds to wait before swapping back to previous slot", 200, 50, 2000));

    private final BoolSetting onBreak = register(new BoolSetting(
            "On Break", "Swap back after breaking a block", true));

    private int previousSlot = -1;
    private int changedSlot = -1;
    private final TimerUtil timer = new TimerUtil();
    private boolean waiting = false;

    public SwapBack() {
        super("SwapBack", "Swaps back to previous slot after use", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        previousSlot = -1;
        changedSlot = -1;
        waiting = false;
    }

    @Override
    public void onDisable() {
        previousSlot = -1;
        changedSlot = -1;
        waiting = false;
    }

    /** Call this from other modules or key handler when you switch slots. */
    public void notifySwitch(int from, int to) {
        previousSlot = from;
        changedSlot = to;
        waiting = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        int current = mc.player.getInventory().selectedSlot;

        // Detect manual slot change
        if (previousSlot == -1) {
            previousSlot = current;
            changedSlot = current;
            return;
        }

        if (current != changedSlot && current != previousSlot) {
            // Player switched to a third slot — update tracking
            previousSlot = changedSlot;
            changedSlot = current;
            waiting = false;
            return;
        }

        // When the player is no longer using an item, start the timer
        if (!mc.player.isUsingItem() && current == changedSlot && previousSlot != changedSlot) {
            if (!waiting) {
                timer.reset();
                waiting = true;
            }
            if (timer.hasReached(delay.get())) {
                mc.player.getInventory().selectedSlot = previousSlot;
                changedSlot = previousSlot;
                previousSlot = -1;
                waiting = false;
            }
        } else if (mc.player.isUsingItem()) {
            waiting = false;
        }
    }
}
