package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.waypoint.WaypointManager;
import cc.quark.waypoint.WaypointManager.Waypoint;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class WaypointCommand extends Command {

    private final WaypointManager waypointManager;

    public WaypointCommand(WaypointManager waypointManager) {
        super("waypoint", "Manage waypoints.", "waypoint <add <name>|list|remove <name>|clear>");
        this.waypointManager = waypointManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            reply("§cUsage: .waypoint <add <name>|list|remove <name>|clear>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length < 2) { reply("§cUsage: .waypoint add <name>"); return; }
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null) { reply("§cNot in a world."); return; }
                String name = args[1];
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                waypointManager.add(name, x, y, z);
                reply("§aWaypoint §f'" + name + "' §aadded at §f"
                        + String.format("%.1f, %.1f, %.1f", x, y, z) + "§a.");
            }
            case "remove" -> {
                if (args.length < 2) { reply("§cUsage: .waypoint remove <name>"); return; }
                waypointManager.remove(args[1]);
                reply("§aRemoved waypoint §f'" + args[1] + "' §a(if it existed).");
            }
            case "clear" -> {
                waypointManager.clear();
                reply("§aAll waypoints cleared.");
            }
            case "list" -> {
                List<Waypoint> list = waypointManager.getWaypoints();
                if (list.isEmpty()) { reply("§7No waypoints saved."); return; }
                replyRaw("§8--- §bWaypoints §8(" + list.size() + ") ---");
                for (Waypoint w : list) {
                    replyRaw("  §b" + w.name() + " §8- §f"
                            + String.format("%.1f, %.1f, %.1f", w.x(), w.y(), w.z()));
                }
            }
            default -> reply("§cUnknown sub-command. Use add/list/remove/clear.");
        }
    }
}
