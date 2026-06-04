package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class QuickSwap extends Module {

    private final IntSetting keybind = register(new IntSetting(
            "Keybind", "GLFW key code to trigger swap (0 = auto on tick)", 0, 0, 400));

    private final TimerUtil timer = new TimerUtil();
    private static final long SWAP_COOLDOWN = 300L;

    public QuickSwap() {
        super("QuickSwap", "Quickly swaps main and offhand items", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(SWAP_COOLDOWN)) return;

        // When keybind is 0 (none), swap immediately when module is toggled on
        if (keybind.get() == 0) {
            swapHands();
            timer.reset();
            this.disable(); // Toggle off after swap
        }
        // Otherwise the EventKey handler (not implemented here) would call swapHands()
    }

    public void swapHands() {
        if (mc.player == null || mc.interactionManager == null) return;
        // Slot F (swap with offhand) is key action, use slot action
        int syncId = mc.player.currentScreenHandler.syncId;
        int hotbarSlot = mc.player.getInventory().selectedSlot;
        mc.interactionManager.clickSlot(syncId, hotbarSlot + 36, 40, SlotActionType.SWAP, mc.player);
    }
}
