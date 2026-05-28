package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

public class AutoRegister extends Module {

    private final ModeSetting password = register(new ModeSetting("Password", "Password to use for register/login", "Password123", "Password123", "quark1234", "admin123", "password1"));
    private final BoolSetting autoLogin = register(new BoolSetting("Auto Login", "Also auto-login when prompted", true));
    private final TimerUtil timer = new TimerUtil();

    public AutoRegister() {
        super("AutoRegister", "Auto-registers and logs in on servers that require it", Category.PLAYER);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (!timer.hasReached(3000)) return;

        String msg = event.getMessage().toLowerCase();
        String pass = password.get();

        if (msg.contains("register") && (msg.contains("please") || msg.contains("use /register") || msg.contains("/register"))) {
            if (mc.player != null) {
                ChatUtil.send("/register " + pass + " " + pass);
                ChatUtil.info("Auto-registered with password.");
                timer.reset();
            }
        } else if (autoLogin.isEnabled() && (msg.contains("login") || msg.contains("/login")) && !msg.contains("register")) {
            if (mc.player != null) {
                ChatUtil.send("/login " + pass);
                ChatUtil.info("Auto-logged in.");
                timer.reset();
            }
        }
    }
}
