package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class UseOnSelf extends Module {

    public UseOnSelf() {
        super("UseOnSelf", "Uses held item on self when key pressed (useful for splash potions)", Category.PLAYER);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() != GLFW.GLFW_KEY_U) return;
        if (mc.player == null || mc.interactionManager == null) return;

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
    }
}
