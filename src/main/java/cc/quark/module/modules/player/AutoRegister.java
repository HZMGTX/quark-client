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

    private final ModeSetting password = register(new ModeSetting(
            "Password", "Password to use for register/login",
            "Password123", "Password123", "quark1234", "admin123", "password1"));
    private final BoolSetting autoLogin = register(new BoolSetting(
            "Auto Login", "Also respond to login prompts", true));
    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Show notification when command is sent", true));

    private final TimerUtil cooldown = new TimerUtil();

    public AutoRegister() {
        super("AutoRegister", "Auto-registers and logs in on servers that require it", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        cooldown.reset();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;
        if (!cooldown.hasReached(3000)) return;

        String msg = event.getMessage().toLowerCase();
        String pass = password.get();

        boolean isRegister = msg.contains("register")
                && (msg.contains("please") || msg.contains("/register") || msg.contains("use"));
        boolean isLogin = msg.contains("login")
                && (msg.contains("please") || msg.contains("/login") || msg.contains("use"));

        if (isRegister) {
            ChatUtil.send("/register " + pass + " " + pass);
            if (notify.isEnabled()) ChatUtil.success("Auto-registered with password.");
            cooldown.reset();
        } else if (isLogin && autoLogin.isEnabled()) {
            ChatUtil.send("/login " + pass);
            if (notify.isEnabled()) ChatUtil.success("Auto-logged in.");
            cooldown.reset();
        }
    }
}
