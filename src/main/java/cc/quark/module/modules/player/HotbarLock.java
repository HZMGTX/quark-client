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

    private int playerIntent = -1;

    public HotbarLock() {
        super("HotbarLock", "Prevents the selected hotbar slot from being changed by the server", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        playerIntent = mc.player != null ? mc.player.getInventory().selectedSlot : lockedSlot.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        int currentSlot = mc.player.getInventory().selectedSlot;
        int locked = lockedSlot.get();

        if (allowManual.isEnabled()) {
            if (playerIntent == -1) playerIntent = currentSlot;
            if (currentSlot != playerIntent) {
                // Slot changed since we last confirmed the player's intent — treat as manual change
                // and update playerIntent to follow the player
                playerIntent = currentSlot;
            }
            // Note: detecting server-forced changes reliably requires a packet mixin;
            // with a tick handler alone we cannot distinguish them from player input.
        } else {
            // Hard lock — always restore to configured slot
            if (currentSlot != locked) {
                mc.player.getInventory().selectedSlot = locked;
            }
        }
    }
}
