package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.friend.FriendManager;

import java.util.Set;

/**
 * Manage the friends list so modules (like KillAura) skip them.
 * Usage: .friend <add|remove|list> [username]
 */
public class FriendCommand extends Command {

    private final FriendManager friendManager;

    public FriendCommand(FriendManager friendManager) {
        super("friend", "Manage the friends list.", "friend <add|remove|list> [username]");
        this.friendManager = friendManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            reply("Â§cUsage: .friend <add|remove|list> [username]");
            return;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add" -> {
                if (args.length < 2) { reply("Â§cPlease specify a username."); return; }
                String name = args[1];
                if (friendManager.isFriend(name)) {
                    reply(name + " is already a friend.");
                } else {
                    friendManager.addFriend(name);
                    reply("Â§aAdded Â§r" + name + " Â§ato friends.");
                }
            }
            case "remove", "del", "rm" -> {
                if (args.length < 2) { reply("Â§cPlease specify a username."); return; }
                String name = args[1];
                if (!friendManager.isFriend(name)) {
                    reply(name + " is not in your friends list.");
                } else {
                    friendManager.removeFriend(name);
                    reply("Â§cRemoved Â§r" + name + " Â§cfrom friends.");
                }
            }
            case "list", "ls" -> {
                Set<String> friends = friendManager.getFriends();
                if (friends.isEmpty()) {
                    reply("Your friends list is empty.");
                } else {
                    reply("Â§bFriends (" + friends.size() + ")Â§r: " + String.join(", ", friends));
                }
            }
            default -> reply("Â§cUnknown sub-command: " + sub + ". Use add, remove, or list.");
        }
    }
}
