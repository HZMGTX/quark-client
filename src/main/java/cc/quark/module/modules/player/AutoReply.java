package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;

/**
 * AutoReply - automatically replies to incoming private messages.
 *
 * Detects messages matching common /msg, /tell, and /w patterns and sends a
 * configurable reply via the /r shorthand command.
 */
public class AutoReply extends Module {

    private final ModeSetting reply = register(new ModeSetting(
            "Reply", "Message to send back", "I'm AFK",
            "I'm AFK", "I'm busy", "Custom"));

    private final BoolSetting onlyPrivate = register(new BoolSetting(
            "Only Private", "Only reply to /msg or /w messages", true));

    private final IntSetting cooldown = register(new IntSetting(
            "Cooldown", "Seconds between auto-replies", 30, 5, 120));

    private static final String CUSTOM_REPLY = "I'm using Quark client";

    private final TimerUtil timer = new TimerUtil();

    public AutoReply() {
        super("AutoReply", "Auto-replies to direct messages", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;

        String msg = event.getMessage();

        // Check if the message looks like a private message directed at us
        if (onlyPrivate.isEnabled() && !isPrivateMessage(msg)) return;

        // Respect cooldown
        if (!timer.hasReached(cooldown.get() * 1000L)) return;

        String replyMsg = switch (reply.get()) {
            case "I'm AFK"  -> "I'm AFK right now.";
            case "I'm busy" -> "I'm busy, please message me later.";
            default         -> CUSTOM_REPLY;
        };

        mc.player.networkHandler.sendChatMessage("/r " + replyMsg);
        timer.reset();
    }

    /**
     * Returns true if the message matches common private-message patterns:
     *   [player -> you]: message
     *   player whispers to you: message
     *   [player -> me]: message
     */
    private boolean isPrivateMessage(String msg) {
        String lower = msg.toLowerCase();
        return lower.contains("-> you")
                || lower.contains("whispers to you")
                || lower.contains("[msg]")
                || lower.contains("[pm]")
                || lower.contains("[dm]")
                || lower.contains("[tell]")
                || (lower.contains("->") && lower.contains("you"));
    }
}
