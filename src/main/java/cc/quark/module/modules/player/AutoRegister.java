package cc.quark.module.modules.player;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

/**
 * AutoRegister - sends a /register command with the configured password on enable.
 */
public class AutoRegister extends Module {

    private final BoolSetting notify = register(new BoolSetting("Notify", "Print confirmation to chat", true));

    public AutoRegister() {
        super("AutoRegister", "Sends a register command on enable", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        ChatUtil.send("/register password password");
        if (notify.isEnabled()) ChatUtil.success("Sent register command");
    }
}
