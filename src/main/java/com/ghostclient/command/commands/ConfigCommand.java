package com.ghostclient.command.commands;

import com.ghostclient.command.Command;
import com.ghostclient.config.ConfigManager;

/**
 * Saves or loads the GhostClient configuration.
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
            reply("§cUsage: .config <save|load>");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "save" -> {
                configManager.save();
                reply("§aConfiguration saved.");
            }
            case "load" -> {
                configManager.load();
                reply("§aConfiguration loaded.");
            }
            default -> reply("§cUnknown option: " + args[0] + ". Use save or load.");
        }
    }
}
