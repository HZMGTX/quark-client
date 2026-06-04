package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;

public class EmptyHanded extends Module {

    private final IntSetting emptySlot = register(new IntSetting(
            "Empty Slot", "Hotbar slot index (1-9) that should always be kept empty", 8, 1, 9));

    private final BoolSetting onSneak = register(new BoolSetting(
            "On Sneak", "Only switch to empty slot while sneaking", false));

    private int previousSlot = -1;

    public EmptyHanded() {
        super("EmptyHanded", "Switches to empty hand slot automatically", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = previousSlot;
            previousSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean shouldSwitch = !onSneak.isEnabled() || mc.player.isSneaking();

        if (!shouldSwitch) {
            if (previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                previousSlot = -1;
            }
            return;
        }

        int targetSlot = Math.min(8, Math.max(0, emptySlot.get() - 1));
        ItemStack stackInSlot = mc.player.getInventory().getStack(targetSlot);

        // Only switch if that slot is actually empty
        if (!stackInSlot.isEmpty()) return;

        int current = mc.player.getInventory().selectedSlot;
        if (current != targetSlot) {
            if (previousSlot == -1) previousSlot = current;
            mc.player.getInventory().selectedSlot = targetSlot;
        }
    }
}
