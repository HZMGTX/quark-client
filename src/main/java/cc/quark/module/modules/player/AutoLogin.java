package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;

public class AutoLogin extends Module {

    private final ModeSetting password = register(new ModeSetting(
            "Password", "Password to use",
            "1234", "1234", "password", "quark", "admin", "123456"));
    private final BoolSetting autoRegister = register(new BoolSetting(
            "Auto Register", "Also auto-register when prompted", true));
    private final BoolSetting notifyOnSend = register(new BoolSetting(
            "Notify", "Show a chat notification after sending", true));

    private final TimerUtil cooldown = new TimerUtil();

    public AutoLogin() {
        super("AutoLogin", "Listens for login/register prompts and auto-responds", Category.PLAYER);
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

        boolean isLoginPrompt  = msg.contains("/login")   || (msg.contains("login")   && msg.contains("please"));
        boolean isRegisterPrompt = msg.contains("/register") || (msg.contains("register") && msg.contains("please"));

        if (isRegisterPrompt && autoRegister.isEnabled()) {
            ChatUtil.send("/register " + pass + " " + pass);
            if (notifyOnSend.isEnabled()) ChatUtil.info("Auto-registered.");
            cooldown.reset();
        } else if (isLoginPrompt) {
            ChatUtil.send("/login " + pass);
            if (notifyOnSend.isEnabled()) ChatUtil.info("Auto-logged in.");
            cooldown.reset();
        }
    }
}
