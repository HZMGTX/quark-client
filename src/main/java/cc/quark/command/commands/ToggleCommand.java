package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.module.Module;
import cc.quark.module.ModuleManager;

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
            reply("Â§cUsage: .toggle <module>");
            return;
        }

        String name = args[0];
        Module module = moduleManager.getModule(name);

        if (module == null) {
            reply("Â§cModule not found: " + name);
            return;
        }

        module.toggle();
        reply(module.getName() + " is now " + (module.isEnabled() ? "Â§aenabled" : "Â§cdisabled") + "Â§r.");
    }
}
