package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

/**
 * InvMove2 - allows moving while a GUI (inventory, chest, etc.) is open by
 * forwarding movement key states through to the game even when a screen is active.
 */
public class InvMove2 extends Module {

    private final BoolSetting allowMovement = register(new BoolSetting(
            "AllowMovement", "Forward WASD movement keys while a GUI is open", true));

    public InvMove2() {
        super("InvMove2", "Move while inventory is open", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        // Release any forced keys on disable
        if (mc.player != null) {
            mc.options.forwardKey.setPressed(false);
            mc.options.backKey.setPressed(false);
            mc.options.leftKey.setPressed(false);
            mc.options.rightKey.setPressed(false);
            mc.options.jumpKey.setPressed(false);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!allowMovement.isEnabled()) return;

        Screen screen = mc.currentScreen;
        if (!(screen instanceof HandledScreen<?>)) return;

        // Mirror the physical key state into the game's key bindings
        // so that movement is processed even while the GUI is open.
        mc.options.forwardKey.setPressed(
                org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(),
                        mc.options.forwardKey.getDefaultKey().getCode()) == org.lwjgl.glfw.GLFW.GLFW_PRESS);
        mc.options.backKey.setPressed(
                org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(),
                        mc.options.backKey.getDefaultKey().getCode()) == org.lwjgl.glfw.GLFW.GLFW_PRESS);
        mc.options.leftKey.setPressed(
                org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(),
                        mc.options.leftKey.getDefaultKey().getCode()) == org.lwjgl.glfw.GLFW.GLFW_PRESS);
        mc.options.rightKey.setPressed(
                org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(),
                        mc.options.rightKey.getDefaultKey().getCode()) == org.lwjgl.glfw.GLFW.GLFW_PRESS);
        mc.options.jumpKey.setPressed(
                org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(),
                        mc.options.jumpKey.getDefaultKey().getCode()) == org.lwjgl.glfw.GLFW.GLFW_PRESS);
    }
}
