package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class TimeWeatherAdmin extends Module {

    private final ModeSetting action = register(new ModeSetting(
            "Action", "Which command to run on enable",
            "Day", "Day", "Night", "Sunrise", "Noon", "Rain", "Thunder", "Clear", "Reset"));
    private final StringSetting dayCommand = register(new StringSetting(
            "Day Command", "Command for setting day time", "time set day"));
    private final StringSetting nightCommand = register(new StringSetting(
            "Night Command", "Command for setting night time", "time set night"));
    private final StringSetting clearCommand = register(new StringSetting(
            "Clear Command", "Command for clearing weather", "weather clear"));
    private final StringSetting rainCommand = register(new StringSetting(
            "Rain Command", "Command for starting rain", "weather rain"));
    private final StringSetting thunderCommand = register(new StringSetting(
            "Thunder Command", "Command for starting thunder", "weather thunder"));

    public TimeWeatherAdmin() {
        super("TimeWeatherAdmin", "Quick-fires time and weather commands with configurable presets", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }

        String cmd = resolveCommand();
        if (cmd == null) {
            ChatUtil.warn("[TimeWeather] Unknown action selected.");
            disable();
            return;
        }
        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.info("§6[TimeWeather] §fExecuted: §e/" + cmd);
        disable();
    }

    private String resolveCommand() {
        return switch (action.get()) {
            case "Day"     -> dayCommand.get();
            case "Night"   -> nightCommand.get();
            case "Sunrise" -> "time set 0";
            case "Noon"    -> "time set 6000";
            case "Rain"    -> rainCommand.get();
            case "Thunder" -> thunderCommand.get();
            case "Clear"   -> clearCommand.get();
            case "Reset"   -> "time set 0";
            default        -> null;
        };
    }

    @Override
    public String getSuffix() {
        return action.get();
    }
}
