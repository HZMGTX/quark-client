package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.ModuleManager;

import java.util.List;

public class ModulesCommand extends Command {

    private final ModuleManager moduleManager;

    public ModulesCommand(ModuleManager moduleManager) {
        super("modules", "List modules, optionally filtered by category.", "modules [category]");
        this.moduleManager = moduleManager;
    }

    @Override
    public void execute(String[] args) {
        Category filter = null;
        if (args.length > 0) {
            for (Category c : Category.values()) {
                if (c.name().equalsIgnoreCase(args[0]) || c.getDisplayName().equalsIgnoreCase(args[0])) {
                    filter = c;
                    break;
                }
            }
            if (filter == null) {
                reply("§cUnknown category: " + args[0] + ". Valid: combat, movement, render, player, world, exploit, misc");
                return;
            }
        }

        final Category finalFilter = filter;
        List<Module> modules = moduleManager.getModules().stream()
                .filter(m -> finalFilter == null || m.getCategory() == finalFilter)
                .filter(m -> !m.getName().startsWith("Reserved-"))
                .toList();

        String header = filter == null ? "§8--- §bAll Modules §8(" + modules.size() + ") ---"
                : "§8--- §b" + filter.getDisplayName() + " §8(" + modules.size() + ") ---";
        replyRaw(header);

        for (Module m : modules) {
            if (m.isEnabled()) {
                replyRaw("  §a[ON] §f" + m.getName());
            } else {
                replyRaw("  §7[OFF] §8" + m.getName());
            }
        }
    }
}
