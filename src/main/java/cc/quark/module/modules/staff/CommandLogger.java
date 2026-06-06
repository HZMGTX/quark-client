package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class CommandLogger extends Module {
    private final BoolSetting alerts = register(new BoolSetting("Alerts", "Send alerts to staff", true));
    private final StringSetting logLevel = register(new StringSetting("LogLevel", "Logging verbosity", "Normal"));

    public CommandLogger() { super("CommandLogger", "Logs all commands issued by players", Category.STAFF); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
    }
}
