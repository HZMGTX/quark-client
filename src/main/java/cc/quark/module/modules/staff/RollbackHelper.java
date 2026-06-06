package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

/**
 * RollbackHelper - Helps plan and execute rollback commands for common
 * block-logging plugins (CoreProtect, LogBlock, Prism).
 * Generates and optionally sends the rollback command based on settings.
 */
public class RollbackHelper extends Module {

    private final ModeSetting plugin = register(new ModeSetting(
            "Plugin", "Target rollback plugin", "CoreProtect",
            "CoreProtect", "LogBlock", "Prism"));

    private final StringSetting targetPlayer = register(new StringSetting(
            "Player", "Player whose actions to roll back", ""));

    private final IntSetting timeValue = register(new IntSetting(
            "Time Value", "Amount of time for rollback", 1, 1, 720));

    private final ModeSetting timeUnit = register(new ModeSetting(
            "Time Unit", "Unit of time for rollback", "h",
            "s", "m", "h", "d", "w"));

    private final IntSetting radiusBlocks = register(new IntSetting(
            "Radius", "Block radius for rollback (0 = global)", 10, 0, 1000));

    private final BoolSetting includeBlocks = register(new BoolSetting(
            "Blocks", "Include block place/break in rollback", true));

    private final BoolSetting includeContainers = register(new BoolSetting(
            "Containers", "Include container interactions in rollback", true));

    private final BoolSetting includeKills = register(new BoolSetting(
            "Kills", "Include entity kills in rollback", false));

    private final BoolSetting dryRun = register(new BoolSetting(
            "Preview Only", "Send a preview/inspect command instead of actual rollback", true));

    private final BoolSetting executeOnEnable = register(new BoolSetting(
            "Execute On Enable", "Send the generated command when module is toggled on", false));

    private boolean executed = false;

    public RollbackHelper() {
        super("RollbackHelper", "Generates and executes rollback commands for logging plugins", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        executed = false;
        String cmd = buildCommand();
        ChatUtil.info("[RollbackHelper] Generated: /" + cmd);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (executed || mc.player == null) return;
        if (!executeOnEnable.isEnabled()) return;

        executed = true;
        String cmd = buildCommand();
        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.success("[RollbackHelper] Sent: /" + cmd);
        disable();
    }

    private String buildCommand() {
        String player = targetPlayer.get().trim();
        int r = radiusBlocks.get();
        String time = timeValue.get() + timeUnit.get();

        return switch (plugin.get()) {
            case "CoreProtect" -> buildCoreProtect(player, time, r);
            case "LogBlock"    -> buildLogBlock(player, time, r);
            case "Prism"       -> buildPrism(player, time, r);
            default            -> buildCoreProtect(player, time, r);
        };
    }

    private String buildCoreProtect(String player, String time, int r) {
        StringBuilder sb = new StringBuilder(dryRun.isEnabled() ? "co inspect" : "co rollback");
        if (!player.isEmpty()) sb.append(" u:").append(player);
        sb.append(" t:").append(time);
        if (r > 0) sb.append(" r:").append(r);

        StringBuilder a = new StringBuilder();
        if (includeBlocks.isEnabled())     appendArg(a, "block");
        if (includeContainers.isEnabled()) appendArg(a, "container");
        if (includeKills.isEnabled())      appendArg(a, "kill");
        if (a.length() > 0) sb.append(" a:").append(a);

        return sb.toString();
    }

    private String buildLogBlock(String player, String time, int r) {
        StringBuilder sb = new StringBuilder(dryRun.isEnabled() ? "lb area" : "lb rollback");
        if (!player.isEmpty()) sb.append(" player ").append(player);
        sb.append(" since ").append(time);
        if (r > 0) sb.append(" radius ").append(r);
        return sb.toString();
    }

    private String buildPrism(String player, String time, int r) {
        StringBuilder sb = new StringBuilder(dryRun.isEnabled() ? "prism lookup" : "prism rollback");
        if (!player.isEmpty()) sb.append(" p:").append(player);
        sb.append(" t:").append(time);
        if (r > 0) sb.append(" r:").append(r);
        return sb.toString();
    }

    private void appendArg(StringBuilder sb, String val) {
        if (sb.length() > 0) sb.append("+");
        sb.append(val);
    }
}
