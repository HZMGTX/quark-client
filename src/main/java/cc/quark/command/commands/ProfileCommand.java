package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.config.ConfigManager;

import java.util.List;

public class ProfileCommand extends Command {

    private final ConfigManager configManager;

    public ProfileCommand(ConfigManager configManager) {
        super("profile", "Save/load named config profiles.", "profile <save|load|list|delete> [name]");
        this.configManager = configManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            reply("§cUsage: .profile <save|load|list|delete> [name]");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "save" -> {
                if (args.length < 2) { reply("§cUsage: .profile save <name>"); return; }
                String name = args[1];
                configManager.saveProfile(name);
                reply("§aProfile §f" + name + " §asaved.");
            }
            case "load" -> {
                if (args.length < 2) { reply("§cUsage: .profile load <name>"); return; }
                String name = args[1];
                List<String> profiles = configManager.listProfiles();
                if (!profiles.contains(name)) {
                    reply("§cProfile §f" + name + " §cdoes not exist. Use §f.profile list§c to see available profiles.");
                    return;
                }
                configManager.loadProfile(name);
                reply("§aProfile §f" + name + " §aloaded.");
            }
            case "list" -> {
                List<String> profiles = configManager.listProfiles();
                if (profiles.isEmpty()) {
                    reply("§7No saved profiles.");
                } else {
                    reply("§aSaved profiles: §f" + String.join("§7, §f", profiles));
                }
            }
            case "delete" -> {
                if (args.length < 2) { reply("§cUsage: .profile delete <name>"); return; }
                String name = args[1];
                boolean deleted = configManager.deleteProfile(name);
                if (deleted) {
                    reply("§aProfile §f" + name + " §adeleted.");
                } else {
                    reply("§cProfile §f" + name + " §cnot found.");
                }
            }
            default -> reply("§cUnknown option: §f" + args[0] + "§c. Use save, load, list, or delete.");
        }
    }
}
