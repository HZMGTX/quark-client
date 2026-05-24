package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.command.CommandManager;

/**
 * Lists all registered commands with their descriptions.
 * Usage: .help [page]
 */
public class HelpCommand extends Command {

    private final CommandManager commandManager;

    public HelpCommand(CommandManager commandManager) {
        super("help", "Lists available commands.", "help [page]");
        this.commandManager = commandManager;
    }

    @Override
    public void execute(String[] args) {
        int page = 1;
        if (args.length > 0) {
            try { page = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }

        int perPage = 8;
        int totalPages = (int) Math.ceil(commandManager.getCommands().size() / (double) perPage);
        page = Math.max(1, Math.min(page, totalPages));

        replyRaw("Â§8--- Â§bQuark Commands Â§7(Page " + page + "/" + totalPages + ")Â§8 ---");

        int start = (page - 1) * perPage;
        int end   = Math.min(start + perPage, commandManager.getCommands().size());

        for (int i = start; i < end; i++) {
            Command cmd = commandManager.getCommands().get(i);
            replyRaw("  Â§b." + cmd.getUsage() + " Â§8- Â§7" + cmd.getDescription());
        }
    }
}
