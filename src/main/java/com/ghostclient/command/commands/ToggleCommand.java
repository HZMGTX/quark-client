package com.ghostclient.command.commands;

import com.ghostclient.command.Command;
import com.ghostclient.module.Module;
import com.ghostclient.module.ModuleManager;

/**
 * Toggles a module on or off.
 * Usage: .toggle <module>
 */
public class ToggleCommand extends Command {

    private final ModuleManager moduleManager;

    public ToggleCommand(ModuleManager moduleManager) {
        super("toggle", "Toggle a module on/off.", "toggle <module>");
        this.moduleManager = moduleManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            reply("§cUsage: .toggle <module>");
            return;
        }

        String name = args[0];
        Module module = moduleManager.getModule(name);

        if (module == null) {
            reply("§cModule not found: " + name);
            return;
        }

        module.toggle();
        reply(module.getName() + " is now " + (module.isEnabled() ? "§aenabled" : "§cdisabled") + "§r.");
    }
}
