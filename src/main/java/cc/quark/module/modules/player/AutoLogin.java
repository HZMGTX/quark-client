package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;

public class AutoLogin extends Module {

    private final ModeSetting command = register(new ModeSetting(
            "Command", "Login command format to use",
            "/login", "/login", "/l", "/register"));

    private final ModeSetting password = register(new ModeSetting(
            "Password", "Password to send with the command",
            "1234", "1234", "password", "quark", "admin", "123456"));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks to wait before sending the command (20 = 1 second)", 40, 20, 200));

    private boolean triggered   = false;
    private int     tickCounter = 0;

    public AutoLogin() {
        super("AutoLogin", "Automatically sends a login or register command on joining a server", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        triggered   = false;
        tickCounter = 0;
    }

    @Override
    public void onDisable() {
        triggered   = false;
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || triggered) return;

        tickCounter++;
        if (tickCounter < delay.get()) return;

        String cmd = command.get() + " " + password.get();
        ChatUtil.send(cmd);
        ChatUtil.info("Sent: " + cmd);
        triggered = true;
    }
}
