package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.config.ConfigManager;

/**
 * Saves or loads the Quark configuration.
 * Usage: .config <save|load>
 */
public class ConfigCommand extends Command {

    private final ConfigManager configManager;

    public ConfigCommand(ConfigManager configManager) {
        super("config", "Save or load the client configuration.", "config <save|load>");
        this.configManager = configManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            reply("Â§cUsage: .config <save|load>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "save" -> {
                configManager.save();
                reply("Â§aConfiguration saved.");
            }
            case "load" -> {
                configManager.load();
                reply("Â§aConfiguration loaded.");
            }
            default -> reply("Â§cUnknown option: " + args[0] + ". Use save or load.");
        }
    }
}
