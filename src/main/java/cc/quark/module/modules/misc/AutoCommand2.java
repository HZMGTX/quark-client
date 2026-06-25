package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class AutoCommand2 extends Module {
    private final StringSetting command = register(new StringSetting("Command", "Command to execute", "/afk"));
    private final IntSetting interval = register(new IntSetting("Interval", "Seconds between commands", 60, 5, 3600));
    private final BoolSetting onlyAFK = register(new BoolSetting("OnlyAFK", "Only run when not moving", false));
    private int tickCount = 0;

    public AutoCommand2() { super("AutoCommand2", "Runs a command on a timer", Category.MISC); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || ++tickCount < interval.getValue() * 20) return;
        tickCount = 0;
        ChatUtil.send(command.getValue());
    }
}
