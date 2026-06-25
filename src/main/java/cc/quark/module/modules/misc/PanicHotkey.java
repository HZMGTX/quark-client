package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.ModuleManager;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import org.lwjgl.glfw.GLFW;

public class PanicHotkey extends Module {

    private final IntSetting panicKey = register(new IntSetting("PanicKey", "Key to trigger panic disable", GLFW.GLFW_KEY_R, GLFW.GLFW_KEY_A, GLFW.GLFW_KEY_LAST));
    private final BoolSetting showMessage = register(new BoolSetting("ShowMessage", "Show message when panic triggered", true));

    public PanicHotkey() {
        super("PanicHotkey", "Single key instantly disables all enabled modules", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKey() != panicKey.getValue()) return;
        ModuleManager mm = cc.quark.Quark.getInstance().getModuleManager();
        if (mm == null) return;
        int count = 0;
        for (Module m : mm.getModules()) {
            if (m != this && m.isEnabled()) {
                m.disable();
                count++;
            }
        }
        if (showMessage.getValue()) ChatUtil.warn("Panic! Disabled " + count + " modules.");
    }
}
