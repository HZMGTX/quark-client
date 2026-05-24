package cc.quark.command;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Abstract base class for all Quark chat commands.
 * Commands are triggered when the player sends a chat message starting with
 * the CommandManager's prefix (default ".").
 */
public abstract class Command {

    private final String name;
    private final String description;
    private final String usage;

    /**
     * @param name        the command name (without prefix), e.g. "bind"
     * @param description short human-readable description shown in .help
     * @param usage       usage hint, e.g. "bind <module> <key>"
     */
    protected Command(String name, String description, String usage) {
        this.name = name.toLowerCase();
        this.description = description;
        this.usage = usage;
    }

    protected Command(String name, String description) {
        this(name, description, name);
    }

    // -------------------------------------------------------------------------
    // Abstract
    // -------------------------------------------------------------------------

    /**
     * Execute this command.
     *
     * @param args the arguments following the command name (already split on spaces)
     */
    public abstract void execute(String[] args);

    // -------------------------------------------------------------------------
    // Helpers available to all commands
    // -------------------------------------------------------------------------

    /** Send a chat message visible only to the local player (not sent to server). */
    protected void reply(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("Â§7[Â§bGhostÂ§7] Â§r" + message), false);
        }
    }

    /** Send a plain (no prefix) message to the local player. */
    protected void replyRaw(String message) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(message), false);
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }
}
