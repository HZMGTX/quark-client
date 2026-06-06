package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * TeleportAll - Teleports all (or filtered) players to the local player's
 * location by sending /tp <player> <x> <y> <z> for each one.
 */
public class TeleportAll extends Module {

    private final BoolSetting useNameSelector = register(new BoolSetting(
            "Use @a Selector", "Send /tp @a <x> <y> <z> in one command instead of per-player", false));

    private final StringSetting excludeList = register(new StringSetting(
            "Exclude", "Comma-separated player names to skip (case-insensitive)", ""));

    private final BoolSetting excludeSelf = register(new BoolSetting(
            "Exclude Self", "Skip teleporting the local player", true));

    private final IntSetting delayTicks = register(new IntSetting(
            "Delay", "Ticks between each /tp command (for per-player mode)", 2, 1, 20));

    private final BoolSetting dryRun = register(new BoolSetting(
            "Dry Run", "Print commands without sending them", false));

    private final BoolSetting notifyPlayers = register(new BoolSetting(
            "Notify Players", "Broadcast a message before teleporting", false));

    private final StringSetting notifyMessage = register(new StringSetting(
            "Notify Message", "Message to broadcast before teleporting", "Everyone is being teleported!"));

    private boolean started = false;
    private int ticksSinceStart = 0;
    private List<String> queue = new ArrayList<>();

    public TeleportAll() {
        super("TeleportAll", "Teleports all players to the local player's location", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.getNetworkHandler() == null) {
            ChatUtil.error("[TeleportAll] Not connected.");
            disable();
            return;
        }

        if (notifyPlayers.isEnabled()) {
            String msg = notifyMessage.get().trim();
            if (!msg.isEmpty()) {
                mc.player.networkHandler.sendChatMessage(msg);
            }
        }

        if (useNameSelector.isEnabled()) {
            sendTpCommand("@a");
            ChatUtil.success("[TeleportAll] Sent /tp @a.");
            disable();
            return;
        }

        // Build per-player queue
        queue.clear();
        String excludeRaw = excludeList.get().toLowerCase();
        List<String> excludes = new ArrayList<>();
        for (String e : excludeRaw.split(",")) {
            String t = e.trim();
            if (!t.isEmpty()) excludes.add(t);
        }

        String self = mc.player.getName().getString();
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            String name = entry.getProfile().getName();
            if (excludeSelf.isEnabled() && name.equalsIgnoreCase(self)) continue;
            if (excludes.contains(name.toLowerCase())) continue;
            queue.add(name);
        }

        started = true;
        ticksSinceStart = 0;
        ChatUtil.info("[TeleportAll] Queued " + queue.size() + " players to teleport.");
    }

    @Override
    public void onDisable() {
        started = false;
        queue.clear();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!started || queue.isEmpty() || mc.player == null) {
            if (started && queue.isEmpty()) disable();
            return;
        }

        ticksSinceStart++;
        if (ticksSinceStart < delayTicks.get()) return;
        ticksSinceStart = 0;

        String player = queue.remove(0);
        sendTpCommand(player);
    }

    private void sendTpCommand(String target) {
        if (mc.player == null) return;
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();
        String cmd = String.format("tp %s %.2f %.2f %.2f", target, x, y, z);

        if (dryRun.isEnabled()) {
            ChatUtil.info("[TeleportAll] Dry: /" + cmd);
        } else {
            mc.player.networkHandler.sendChatCommand(cmd);
        }
    }
}
