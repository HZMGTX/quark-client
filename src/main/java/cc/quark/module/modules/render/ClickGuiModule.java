package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Module {

    private final ColorSetting accentColor = register(new ColorSetting(
            "Accent Color", "Global accent color for the client GUI", 0xFF00AAFF));

    public ClickGuiModule() {
        super("ClickGUI", "Settings for the ClickGUI and overall theme", Category.RENDER, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        // Automatically open the GUI when enabled
        if (mc.currentScreen == null) {
            mc.setScreen(new cc.quark.gui.ClickGUI());
        }
        // Immediately toggle off so it doesn't stay "enabled" and cause issues if closed manually
        this.disable();
    }

    public int getAccentColor() {
        return accentColor.get();
    }
}
