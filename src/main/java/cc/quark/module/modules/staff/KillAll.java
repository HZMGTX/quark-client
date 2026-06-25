package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

/**
 * KillAll - Kills all entities of a specified type with a single command.
 * Sends /kill @e[type=<type>] on enable, then auto-disables.
 */
public class KillAll extends Module {

    private final ModeSetting targetMode = register(new ModeSetting(
            "Target", "Preset entity type to kill",
            "Hostile Mobs",
            "Hostile Mobs", "Passive Mobs", "All Mobs", "Items", "Arrows", "Custom"));

    private final StringSetting customType = register(new StringSetting(
            "Custom Type", "Entity type ID when Target is set to Custom (e.g. minecraft:zombie)", "minecraft:zombie"));

    private final BoolSetting excludePlayers = register(new BoolSetting(
            "Exclude Players", "Add type!=player selector argument", true));

    private final BoolSetting dryRun = register(new BoolSetting(
            "Dry Run", "Print the command locally without sending it", false));

    private boolean executed = false;

    public KillAll() {
        super("KillAll", "Kills all entities of the selected type with one /kill command", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        executed = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (executed) return;
        if (mc.player == null) return;

        executed = true;
        String selector = buildSelector();
        String command  = "kill " + selector;

        if (dryRun.isEnabled()) {
            ChatUtil.info("[KillAll] Dry run: /" + command);
        } else {
            mc.player.networkHandler.sendChatCommand(command);
            ChatUtil.success("[KillAll] Executed: /" + command);
        }

        disable();
    }

    private String buildSelector() {
        StringBuilder sb = new StringBuilder("@e[");

        String type = getTypeArg();
        if (!type.isEmpty()) {
            sb.append("type=").append(type);
        }

        if (excludePlayers.isEnabled() && !type.equals("minecraft:player")) {
            if (sb.length() > 3) sb.append(",");
            sb.append("type!=minecraft:player");
        }

        sb.append("]");
        return sb.toString();
    }

    private String getTypeArg() {
        return switch (targetMode.get()) {
            case "Hostile Mobs" -> "#minecraft:raiders"; // approximate; many servers support tag selectors
            case "Passive Mobs" -> "#minecraft:skeletons"; // fallback — use Custom for precise control
            case "All Mobs"     -> "#minecraft:mob";
            case "Items"        -> "minecraft:item";
            case "Arrows"       -> "minecraft:arrow";
            case "Custom"       -> customType.get().trim();
            default             -> "";
        };
    }
}
