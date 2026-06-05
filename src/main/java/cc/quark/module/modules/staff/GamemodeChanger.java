package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;

public class GamemodeChanger extends Module {
    private final ModeSetting mode = register(new ModeSetting("Mode", "Target gamemode", "Creative", "Survival", "Creative", "Adventure", "Spectator"));

    public GamemodeChanger() {
        super("Gamemode", "Quick gamemode switching via command", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String cmd = switch (mode.get()) {
            case "Survival" -> "gamemode survival";
            case "Adventure" -> "gamemode adventure";
            case "Spectator" -> "gamemode spectator";
            default -> "gamemode creative";
        };
        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.info("[Gamemode] Switching to " + mode.get());
        disable();
    }
}
