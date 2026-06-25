package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class MiddleClickPearl extends Module {
    public MiddleClickPearl() {
        super("Middle Click Pearl", "Middle click to throw ender pearl", Category.PLAYER, 0);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null || mc.interactionManager == null) return;
        // Middle mouse button = GLFW_MOUSE_BUTTON_MIDDLE = 2
        if (event.getKeyCode() != 2) return;
        // Find ender pearl in hotbar
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.ENDER_PEARL)) {
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                return;
            }
        }
    }
}
