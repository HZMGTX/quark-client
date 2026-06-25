package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;

/**
 * AutoPrivateMsg - Detects incoming private messages and automatically replies
 * with a configurable message after an optional delay.
 *
 * The module looks for the common "/msg", "/tell", or "/w" reply prefix that
 * most servers use when delivering private messages (e.g. "[Player -> you]").
 * A configurable prefix string allows servers with non-standard PM formats.
 *
 * A cooldown prevents spam when many players message at once.
 */
public class AutoPrivateMsg extends Module {

    private final StringSetting replyMsg   = register(new StringSetting(
            "Reply", "Message to auto-send in reply", "AFK - back shortly!"));

    private final StringSetting pmPrefix   = register(new StringSetting(
            "PM Prefix", "Prefix that indicates an incoming PM (e.g. '[' or 'MSG')", "["));

    private final StringSetting replyCmd   = register(new StringSetting(
            "Reply Cmd", "Command used to reply, e.g. /r or /msg {player}", "/r"));

    private final IntSetting  delay        = register(new IntSetting(
            "Delay", "Milliseconds before sending reply", 500, 0, 10000));

    private final IntSetting  cooldown     = register(new IntSetting(
            "Cooldown", "Minimum ms between replies", 3000, 500, 60000));

    private final BoolSetting requireArrow = register(new BoolSetting(
            "Require Arrow", "Message must contain '->' to qualify as a PM", true));

    private final TimerUtil cooldownTimer = new TimerUtil();
    private final TimerUtil delayTimer    = new TimerUtil();
    private String pendingSender          = null;

    public AutoPrivateMsg() {
        super("AutoPrivateMsg", "Auto-responds to private messages with a configurable reply", Category.MISC);
    }

    @Override
    public void onEnable() {
        pendingSender = null;
        cooldownTimer.reset();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (mc.player == null) return;
        if (!cooldownTimer.hasReached(cooldown.get())) return;

        String msg = event.getMessage();
        if (msg == null) return;

        String lower = msg.toLowerCase();
        boolean hasPrefix = lower.contains(pmPrefix.get().toLowerCase());
        boolean hasArrow  = !requireArrow.isEnabled() || msg.contains("->");

        if (hasPrefix && hasArrow) {
            // Extract sender name from patterns like "[Player -> you]" or "[you -> Player]"
            String sender = extractSender(msg);
            pendingSender = sender;
            delayTimer.reset();
        }
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (pendingSender == null) return;
        if (!delayTimer.hasReached(delay.get())) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        String cmd = replyCmd.get().trim();
        String reply = replyMsg.get().trim();
        if (reply.isEmpty()) { pendingSender = null; return; }

        if (cmd.contains("{player}") && pendingSender != null) {
            cmd = cmd.replace("{player}", pendingSender);
            mc.getNetworkHandler().sendCommand(cmd.substring(1) + " " + reply);
        } else if (cmd.startsWith("/")) {
            mc.getNetworkHandler().sendCommand(cmd.substring(1) + " " + reply);
        } else {
            mc.getNetworkHandler().sendChatMessage(cmd + " " + reply);
        }

        cooldownTimer.reset();
        pendingSender = null;
    }

    /** Tries to pull the sender name out of "[Sender -> you]"-style PM headers. */
    private String extractSender(String msg) {
        int arrow = msg.indexOf("->");
        if (arrow < 0) return null;
        // Walk backwards from arrow to find name start
        int nameEnd = arrow;
        while (nameEnd > 0 && msg.charAt(nameEnd - 1) == ' ') nameEnd--;
        int nameStart = nameEnd;
        while (nameStart > 0 && msg.charAt(nameStart - 1) != '[' && msg.charAt(nameStart - 1) != ' ') {
            nameStart--;
        }
        return nameStart < nameEnd ? msg.substring(nameStart, nameEnd).trim() : null;
    }
}
