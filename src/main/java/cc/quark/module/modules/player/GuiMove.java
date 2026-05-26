package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class GuiMove extends Module {

    private final BoolSetting allowJump = register(new BoolSetting(
            "Jump", "Allow jumping while a GUI is open", true));

    private final BoolSetting ignoreClickGui = register(new BoolSetting(
            "Ignore ClickGUI", "Do not apply movement when the Quark ClickGUI is open", true));

    public GuiMove() {
        super("GuiMove", "Lets you move while a screen is open", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.currentScreen == null) return;

        if (ignoreClickGui.isEnabled()
                && mc.currentScreen instanceof cc.quark.gui.ClickGUI) return;

        long window = mc.getWindow().getHandle();

        boolean forward  = keyDown(window, mc.options.forwardKey.getDefaultKey().getCode());
        boolean backward = keyDown(window, mc.options.backKey.getDefaultKey().getCode());
        boolean left     = keyDown(window, mc.options.leftKey.getDefaultKey().getCode());
        boolean right    = keyDown(window, mc.options.rightKey.getDefaultKey().getCode());
        boolean jump     = allowJump.isEnabled()
                        && keyDown(window, mc.options.jumpKey.getDefaultKey().getCode());

        if (!forward && !backward && !left && !right && !jump) return;

        float yaw    = (float) Math.toRadians(mc.player.getYaw());
        double moveX = 0;
        double moveZ = 0;

        if (forward)  { moveX -= Math.sin(yaw) * 0.15; moveZ += Math.cos(yaw) * 0.15; }
        if (backward) { moveX += Math.sin(yaw) * 0.15; moveZ -= Math.cos(yaw) * 0.15; }
        if (left)     { moveX -= Math.cos(yaw) * 0.15; moveZ -= Math.sin(yaw) * 0.15; }
        if (right)    { moveX += Math.cos(yaw) * 0.15; moveZ += Math.sin(yaw) * 0.15; }

        Vec3d cur = mc.player.getVelocity();
        mc.player.setVelocity(
                cur.x + moveX,
                jump && mc.player.isOnGround() ? 0.42 : cur.y,
                cur.z + moveZ);
    }

    private boolean keyDown(long window, int code) {
        return code >= 0 && GLFW.glfwGetKey(window, code) == GLFW.GLFW_PRESS;
    }
}
