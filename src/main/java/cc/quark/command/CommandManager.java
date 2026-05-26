package cc.quark.command;

import cc.quark.command.commands.*;
import cc.quark.config.ConfigManager;
import cc.quark.friend.FriendManager;
import cc.quark.module.ModuleManager;
import cc.quark.waypoint.WaypointManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Intercepts outgoing chat messages and executes matching commands.
 *
 * <p>The prefix is {@value #PREFIX}.  Commands registered here are invoked
 * when the player types e.g. {@code .help} or {@code .toggle esp} in chat.
 *
 * <p>The mixin {@code MixinClientPlayerEntity} should call
 * {@link #onChat(String)} before the packet is sent, and cancel the packet
 * when this method returns {@code true}.
 */
public class CommandManager {

    private String prefix = ".";
    private static final Logger LOGGER = LoggerFactory.getLogger("Quark/Commands");

    private final List<Command> commands = new ArrayList<>();

    public CommandManager(ModuleManager moduleManager,
                          ConfigManager configManager,
                          FriendManager friendManager,
                          WaypointManager waypointManager) {

        // Register all built-in commands.
        register(new HelpCommand(this));
        register(new ToggleCommand(moduleManager));
        register(new BindCommand(moduleManager));
        register(new FriendCommand(friendManager));
        register(new ConfigCommand(configManager));
        register(new ProfileCommand(configManager));
        register(new VClipCommand());
        register(new HClipCommand());
        register(new PrefixCommand());
        register(new AntiCheatCommand());
        register(new HudCommand());
        register(new SetCommand(moduleManager));
        register(new WaypointCommand(waypointManager));
        register(new ModulesCommand(moduleManager));
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Call this from the chat mixin before the message is sent to the server.
     *
     * @param message raw text the player typed
     * @return {@code true} if the message was a command and should be cancelled
     */
    public boolean onChat(String message) {
        if (message == null || !message.startsWith(prefix)) return false;

        // Strip prefix and split on whitespace.
        String withoutPrefix = message.substring(prefix.length()).trim();
        if (withoutPrefix.isEmpty()) return false;

        String[] parts = withoutPrefix.split("\\s+");
        String name = parts[0].toLowerCase();

        // Build args array (everything after the command name).
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        Command command = getCommand(name);
        if (command == null) {
            sendMessage("§cUnknown command: " + name + ". Type §f.help §cfor a list.");
            return true; // Still cancel the chat â€” it started with prefix.
        }

        try {
            command.execute(args);
        } catch (Exception e) {
            sendMessage("§cError executing command: " + e.getMessage());
            LOGGER.error("Command '{}' threw an exception", name, e);
        }

        return true;
    }

    public void register(Command command) {
        commands.add(command);
    }

    public Command getCommand(String name) {
        return commands.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void setPrefix(String p) { this.prefix = p; }
    public String getPrefix() { return prefix; }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void sendMessage(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(message), false);
        }
    }
}
