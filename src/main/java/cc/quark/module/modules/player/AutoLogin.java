package cc.quark.module.modules.player;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

/**
 * AutoLogin - sends a /login command with the configured password on enable.
 */
public class AutoLogin extends Module {

    private final BoolSetting notify = register(new BoolSetting("Notify", "Print confirmation to chat", true));

    public AutoLogin() {
        super("AutoLogin", "Sends a login command on enable", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        ChatUtil.send("/login password");
        if (notify.isEnabled()) ChatUtil.success("Sent login command");
    }
}
