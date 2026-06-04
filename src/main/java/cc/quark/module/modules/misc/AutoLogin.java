package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

public class AutoLogin extends Module {

    private final StringSetting password = register(new StringSetting("Password", "Password for auto-login command", ""));
    private final StringSetting command  = register(new StringSetting("Command",  "Login command prefix",            "/login"));
    private final IntSetting    delay    = register(new IntSetting   ("Delay",    "Delay before sending (ms)",       2000, 500, 10000));

    private final TimerUtil timer = new TimerUtil();
    private boolean sent = false;

    public AutoLogin() {
        super("AutoLogin", "Auto-types login command on server join", Category.MISC);
    }

    @Override
    public void onEnable() {
        sent = false;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (sent) return;
        if (!timer.hasReached(delay.get())) return;

        String pass = password.get().trim();
        if (pass.isEmpty()) return;

        String full = command.get().trim() + " " + pass;
        // Strip leading / for command sending
        if (full.startsWith("/")) {
            mc.getNetworkHandler().sendCommand(full.substring(1));
        } else {
            mc.getNetworkHandler().sendChatMessage(full);
        }
        sent = true;
        toggle();
    }
}
