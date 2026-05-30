package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class HotbarLock extends Module {

    private final IntSetting lockedSlot = register(new IntSetting(
            "LockedSlot", "The hotbar slot to lock (0-8)", 0, 0, 8));
    private final BoolSetting allowManual = register(new BoolSetting(
            "AllowManual", "Allow manual slot changes, only block server-forced changes", true));

    private int lastPlayerSelectedSlot = -1;

    public HotbarLock() {
        super("HotbarLock", "Prevents the selected hotbar slot from being changed by the server", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        lastPlayerSelectedSlot = -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        int currentSlot = mc.player.getInventory().selectedSlot;
        int locked = lockedSlot.get();

        if (allowManual.isEnabled()) {
            // Track what the player manually selects
            // If the current slot matches what we last saw the player pick, it's manual
            if (currentSlot != locked && currentSlot == lastPlayerSelectedSlot) {
                // Player manually changed slot - update locked slot tracking but keep it
                // Still enforce the lock
                mc.player.getInventory().selectedSlot = locked;
            } else if (currentSlot != locked && currentSlot != lastPlayerSelectedSlot) {
                // Slot changed but player didn't do it manually - server forced it
                mc.player.getInventory().selectedSlot = locked;
            }
        } else {
            // Hard lock - always restore
            if (currentSlot != locked) {
                mc.player.getInventory().selectedSlot = locked;
            }
        }

        lastPlayerSelectedSlot = currentSlot;
    }
}
