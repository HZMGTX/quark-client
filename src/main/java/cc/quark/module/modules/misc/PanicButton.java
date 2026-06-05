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

/**
 * PanicButton — instantly disables all active modules with a configurable hotkey.
 * Distinct from the existing Panic module; this one always stays enabled and acts
 * purely as a passive key listener, never disabling itself.
 */
public class PanicButton extends Module {

    private final IntSetting panicKey = register(new IntSetting(
            "Panic Key", "GLFW key code to trigger panic (default F11=345)", GLFW.GLFW_KEY_F11, 0, 400));

    private final BoolSetting keepHUD = register(new BoolSetting(
            "Keep HUD", "Preserve the HUD module when panicking", true));

    private final BoolSetting keepFullbright = register(new BoolSetting(
            "Keep Fullbright", "Preserve the Fullbright module when panicking", false));

    private final BoolSetting keepThis = register(new BoolSetting(
            "Keep Self", "Keep PanicButton enabled after panic", true));

    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Print a chat message when panic fires", true));

    public PanicButton() {
        super("PanicButton", "Disables all active modules instantly when the panic key is pressed", Category.MISC);
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() != panicKey.get()) return;
        doPanic();
    }

    private void doPanic() {
        Quark q = Quark.getInstance();
        if (q == null) return;

        Module.silent = true;
        int count = 0;
        for (Module m : q.getModuleManager().getModules()) {
            if (!m.isEnabled()) continue;
            if (m == this && keepThis.isEnabled()) continue;
            if (keepHUD.isEnabled() && m.getName().equals("HUD")) continue;
            if (keepFullbright.isEnabled() && m.getName().equals("Fullbright")) continue;
            m.disable();
            count++;
        }
        Module.silent = false;

        if (notify.isEnabled()) {
            ChatUtil.warn("PanicButton: " + count + " module(s) disabled.");
        }
    }
}
