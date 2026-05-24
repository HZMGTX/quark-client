package cc.quark.command.commands;

import cc.quark.Quark;
import cc.quark.command.Command;
import cc.quark.util.ChatUtil;

public class PrefixCommand extends Command {

    public PrefixCommand() {
        super("prefix", "Change the command prefix. Usage: .prefix <char>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) { ChatUtil.error("Usage: .prefix <char>"); return; }
        String newPrefix = args[0].substring(0, 1);
        Quark.getInstance().getCommandManager().setPrefix(newPrefix);
        ChatUtil.success("Command prefix changed to: " + newPrefix);
    }
}
