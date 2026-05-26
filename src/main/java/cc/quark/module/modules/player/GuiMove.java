package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.math.Vec3d;

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

        boolean forward  = isKeyDown(window, mc.options.forwardKey);
        boolean backward = isKeyDown(window, mc.options.backKey);
        boolean left     = isKeyDown(window, mc.options.leftKey);
        boolean right    = isKeyDown(window, mc.options.rightKey);
        boolean jump     = allowJump.isEnabled() && isKeyDown(window, mc.options.jumpKey);

        if (!forward && !backward && !left && !right && !jump) return;

        float yaw  = (float) Math.toRadians(mc.player.getYaw());
        double moveX = 0;
        double moveZ = 0;

        if (forward)  { moveX -= Math.sin(yaw) * 0.15; moveZ += Math.cos(yaw) * 0.15; }
        if (backward) { moveX += Math.sin(yaw) * 0.15; moveZ -= Math.cos(yaw) * 0.15; }
        if (left)     { moveX -= Math.cos(yaw) * 0.15; moveZ -= Math.sin(yaw) * 0.15; }
        if (right)    { moveX += Math.cos(yaw) * 0.15; moveZ += Math.sin(yaw) * 0.15; }

        Vec3d current = mc.player.getVelocity();
        mc.player.setVelocity(
                current.x + moveX,
                jump && mc.player.isOnGround() ? 0.42 : current.y,
                current.z + moveZ);
    }

    private boolean isKeyDown(long window, net.minecraft.client.option.KeyBinding binding) {
        InputUtil.Key key = binding.getDefaultKey();
        if (key.getCategory() == InputUtil.Type.KEYSYM) {
            int code = key.getCode();
            if (code < 0) return false;
            return InputUtil.isKeyOrMouseButtonPressed(window, code);
        }
        return false;
    }
}
