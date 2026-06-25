package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import org.lwjgl.glfw.GLFW;

public class HotkeyManager extends Module {

    public HotkeyManager() {
        super("HotkeyManager", "Assigns commands to function keys; F5=toggle module list", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey event) {
        switch (event.getKeyCode()) {
            case GLFW.GLFW_KEY_F5 -> toggleModuleList();
        }
    }

    private void toggleModuleList() {
        if (mc.player == null) return;
        var mm = Quark.getInstance().getModuleManager();
        var arrayList = mm.getModules().stream()
                .filter(m -> m.getClass().getSimpleName().equals("ArrayList"))
                .findFirst();
        arrayList.ifPresentOrElse(
                m -> { m.toggle(); ChatUtil.info("[HotkeyManager] ArrayList " + (m.isEnabled() ? "ON" : "OFF")); },
                () -> ChatUtil.warn("[HotkeyManager] ArrayList module not found.")
        );
    }
}
