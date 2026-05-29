package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;

public class AutoCommand extends Module {

    private final DoubleSetting interval = register(new DoubleSetting(
            "Interval", "Seconds between each command", 30.0, 1.0, 300.0));
    private final ModeSetting command1 = register(new ModeSetting(
            "Command 1", "First command to send", "/spawn",
            "/spawn", "/home", "/back", "/tp spawn", "/lobby", "none"));
    private final ModeSetting command2 = register(new ModeSetting(
            "Command 2", "Second command to send", "none",
            "/spawn", "/home", "/back", "/tp spawn", "/lobby", "none"));
    private final ModeSetting command3 = register(new ModeSetting(
            "Command 3", "Third command to send", "none",
            "/spawn", "/home", "/back", "/tp spawn", "/lobby", "none"));

    private final TimerUtil timer = new TimerUtil();
    private int commandIndex = 0;

    public AutoCommand() {
        super("AutoCommand", "Auto-sends configured commands at a set interval", Category.MISC);
    }

    @Override
    public void onEnable() {
        commandIndex = 0;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!timer.hasReached((long)(interval.get() * 1000))) return;

        String[] cmds = { command1.get(), command2.get(), command3.get() };
        String cmd = cmds[commandIndex % cmds.length];
        commandIndex = (commandIndex + 1) % cmds.length;

        if (!cmd.equals("none") && !cmd.isEmpty()) {
            mc.player.networkHandler.sendChatMessage(cmd);
        }
        timer.reset();
    }
}
