package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class SlotSave extends Module {

    private final IntSetting savedSlot = register(new IntSetting(
            "SavedSlot", "Hotbar slot index to save and restore (0-8)", 0, 0, 8));

    private final BoolSetting autoRestore = register(new BoolSetting(
            "AutoRestore", "Automatically restore the saved slot each tick", true));

    private int storedSlot = -1;

    public SlotSave() {
        super("SlotSave", "Saves and restores hotbar slot selection", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            storedSlot = mc.player.getInventory().selectedSlot;
        }
    }

    @Override
    public void onDisable() {
        storedSlot = -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        int target = savedSlot.get();

        if (storedSlot == -1) {
            storedSlot = target;
        }

        if (autoRestore.isEnabled()) {
            // Restore saved slot if player changed away from it
            if (mc.player.getInventory().selectedSlot != storedSlot) {
                mc.player.getInventory().selectedSlot = storedSlot;
            }
        }

        // Update stored slot when user explicitly changes the setting
        if (storedSlot != target) {
            storedSlot = target;
        }
    }

    /** Called externally to update the saved slot to the player's current slot. */
    public void saveCurrentSlot() {
        if (mc.player != null) {
            storedSlot = mc.player.getInventory().selectedSlot;
        }
    }
}
