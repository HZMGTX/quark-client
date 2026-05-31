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
            "AllowManual", "When enabled the lock is inactive (all slot changes allowed); disable for hard lock", false));

    public HotbarLock() {
        super("HotbarLock", "Locks the hotbar to a configured slot each tick", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (allowManual.isEnabled()) return;

        int currentSlot = mc.player.getInventory().selectedSlot;
        int locked = lockedSlot.get();
        if (currentSlot != locked) {
            mc.player.getInventory().selectedSlot = locked;
        }
    }
}
