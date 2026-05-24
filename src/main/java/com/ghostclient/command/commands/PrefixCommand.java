package com.ghostclient.command.commands;

import com.ghostclient.GhostClient;
import com.ghostclient.command.Command;
import com.ghostclient.util.ChatUtil;

public class PrefixCommand extends Command {

    public PrefixCommand() {
        super("prefix", "Change the command prefix. Usage: .prefix <char>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) { ChatUtil.error("Usage: .prefix <char>"); return; }
        String newPrefix = args[0].substring(0, 1);
        GhostClient.getInstance().getCommandManager().setPrefix(newPrefix);
        ChatUtil.success("Command prefix changed to: " + newPrefix);
    }
}
