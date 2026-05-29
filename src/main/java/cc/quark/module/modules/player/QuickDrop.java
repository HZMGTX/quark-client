package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;

public class QuickDrop extends Module {

    private final BoolSetting dropOnQ = register(new BoolSetting(
            "Drop on Q", "Drop entire held stack when Q is pressed", true));
    private final BoolSetting rottenFlesh = register(new BoolSetting(
            "Rotten Flesh", "Auto-drop rotten flesh", true));
    private final BoolSetting cobblestone = register(new BoolSetting(
            "Cobblestone", "Auto-drop cobblestone", false));
    private final BoolSetting gravel = register(new BoolSetting(
            "Gravel", "Auto-drop gravel", false));

    private boolean qWasDown = false;
    private final cc.quark.util.TimerUtil dropTimer = new cc.quark.util.TimerUtil();

    public QuickDrop() {
        super("QuickDrop", "Q-drops entire held stack and auto-drops configured junk items", Category.PLAYER);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (!dropOnQ.isEnabled()) return;
        if (event.getKeyCode() == GLFW.GLFW_KEY_Q && mc.player != null && mc.interactionManager != null) {
            // Drop entire held stack
            int slot = mc.player.getInventory().selectedSlot;
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    36 + slot, 1, SlotActionType.THROW, mc.player);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!dropTimer.hasReached(300)) return;
        dropTimer.reset();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !shouldAutoDrop(stack)) continue;
            int guiSlot = i < 9 ? 36 + i : i;
            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    guiSlot, 1, SlotActionType.THROW, mc.player);
        }
    }

    private boolean shouldAutoDrop(ItemStack stack) {
        if (rottenFlesh.isEnabled() && stack.isOf(Items.ROTTEN_FLESH))  return true;
        if (cobblestone.isEnabled() && stack.isOf(Items.COBBLESTONE))   return true;
        if (gravel.isEnabled()      && stack.isOf(Items.GRAVEL))        return true;
        return false;
    }
}
