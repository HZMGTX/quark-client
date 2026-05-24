package com.ghostclient.command.commands;

import com.ghostclient.command.Command;
import com.ghostclient.ghost.GhostManager;
import com.ghostclient.util.ChatUtil;

public class AntiCheatCommand extends Command {

    public AntiCheatCommand() {
        super("ac", "Switch anti-cheat bypass profile. Usage: .ac <profile>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ChatUtil.info("Profiles: vanilla, ncp, aac, grim, watchdog, spartan, matrix, intave");
            ChatUtil.info("Current: " + GhostManager.INSTANCE.getActiveProfile().name());
            return;
        }
        try {
            GhostManager.AntiCheatProfile profile = GhostManager.AntiCheatProfile.valueOf(args[0].toUpperCase());
            GhostManager.INSTANCE.setActiveProfile(profile);
            ChatUtil.success("AC profile set to: " + profile.name());
        } catch (IllegalArgumentException e) {
            ChatUtil.error("Unknown profile: " + args[0]);
            ChatUtil.info("Profiles: vanilla, ncp, aac, grim, watchdog, spartan, matrix, intave");
        }
    }
}
