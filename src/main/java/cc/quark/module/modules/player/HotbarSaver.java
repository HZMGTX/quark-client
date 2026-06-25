package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.ItemStack;

/**
 * HotbarSaver - takes a snapshot of the hotbar and restores it if items are
 * moved out, effectively locking hotbar contents against accidental changes.
 *
 * Full slot-click cancellation requires a mixin; this module provides the
 * lock flag and restores via slot-swap on each tick as a client-side safety net.
 */
public class HotbarSaver extends Module {

    private final BoolSetting lock = register(new BoolSetting(
            "Lock", "Prevent moving items out of the hotbar", true));

    /** Static flag queried by inventory-click mixins to cancel hotbar clicks. */
    public static boolean isLocked = false;

    private ItemStack[] snapshot = null;

    public HotbarSaver() {
        super("HotbarSaver", "Prevents accidentally moving items in hotbar", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        takeSnapshot();
        isLocked = lock.isEnabled();
    }

    @Override
    public void onDisable() {
        isLocked = false;
        snapshot = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        isLocked = lock.isEnabled();

        if (!lock.isEnabled()) return;

        if (snapshot == null) {
            takeSnapshot();
            return;
        }

        // Detect and restore any missing hotbar items
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack current = inv.getStack(i);
            ItemStack saved = snapshot[i];

            if (saved.isEmpty()) continue;
            if (current.isEmpty() || current.getItem() != saved.getItem()) {
                // Find the saved item elsewhere in inventory and swap it back
                for (int j = 9; j < 36; j++) {
                    ItemStack other = inv.getStack(j);
                    if (!other.isEmpty() && other.getItem() == saved.getItem()) {
                        mc.interactionManager.clickSlot(
                                mc.player.currentScreenHandler.syncId,
                                j, i,
                                net.minecraft.screen.slot.SlotActionType.SWAP,
                                mc.player);
                        break;
                    }
                }
            }
        }
    }

    private void takeSnapshot() {
        if (mc.player == null) return;
        snapshot = new ItemStack[9];
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            snapshot[i] = stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
        }
    }

    /** Re-snapshot the current hotbar (e.g. after intentional changes). */
    public void updateSnapshot() {
        takeSnapshot();
    }
}
