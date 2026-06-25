package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class GamepadSupport extends Module {

    private final DoubleSetting deadzone = register(new DoubleSetting("Deadzone", "Analog stick deadzone", 0.15, 0.05, 0.5));
    private final DoubleSetting lookSensitivity = register(new DoubleSetting("LookSens", "Look sensitivity multiplier", 1.0, 0.1, 5.0));
    private final BoolSetting invertY = register(new BoolSetting("InvertY", "Invert vertical look axis", false));

    public GamepadSupport() {
        super("GamepadSupport", "Maps gamepad/controller inputs to Minecraft actions", Category.MISC);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Controller polling via GLFW joystick API
        float[] axes = org.lwjgl.glfw.GLFW.glfwGetJoystickAxes(org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1);
        if (axes == null || axes.length < 4) return;

        float lx = applyDeadzone(axes[0]);
        float ly = applyDeadzone(axes[1]);
        float rx = applyDeadzone(axes[2]);
        float ry = applyDeadzone(invertY.getValue() ? -axes[3] : axes[3]);

        if (Math.abs(rx) > 0 || Math.abs(ry) > 0) {
            float sens = (float) lookSensitivity.getValue();
            mc.player.setYaw(mc.player.getYaw() + rx * sens * 3f);
            mc.player.setPitch(Math.max(-90, Math.min(90, mc.player.getPitch() + ry * sens * 3f)));
        }
    }

    private float applyDeadzone(float v) {
        float d = (float) deadzone.getValue();
        return Math.abs(v) < d ? 0 : v;
    }
}
