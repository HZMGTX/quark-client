package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import org.lwjgl.glfw.GLFW;

public class Panic extends Module {

    private final IntSetting  panicKey      = register(new IntSetting("Panic Key", "GLFW key code for panic (default F12=347)", GLFW.GLFW_KEY_F12, 0, 400));
    private final BoolSetting keepHUD       = register(new BoolSetting("Keep HUD", "Keep HUD module enabled after panic", true));
    private final BoolSetting keepFullbright = register(new BoolSetting("Keep Fullbright", "Keep Fullbright enabled after panic", false));
    private final BoolSetting notify        = register(new BoolSetting("Notify", "Send chat message on panic", true));

    public Panic() {
        super("Panic", "Disables all active modules instantly on a configurable key press", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() != panicKey.get()) return;
        doPanic();
    }

    private void doPanic() {
        Module.silent = true;
        var mm = Quark.getInstance().getModuleManager();
        int count = 0;
        for (Module m : mm.getModules()) {
            if (m == this) continue;
            if (!m.isEnabled()) continue;
            if (keepHUD.isEnabled() && m.getName().equals("HUD")) continue;
            if (keepFullbright.isEnabled() && m.getName().equals("Fullbright")) continue;
            m.disable();
            count++;
        }
        Module.silent = false;
        if (notify.isEnabled()) {
            ChatUtil.info("Panic: disabled " + count + " modules.");
        }
    }

    @Override
    public void onEnable() {
        doPanic();
        this.disable();
    }
}
