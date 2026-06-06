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
 * StatusMessage - Broadcasts a custom status message at a configurable interval.
 *
 * When enabled the module sends a chat message (or command) on a timer, which
 * on many servers will be displayed as a status or away message.  The message
 * can also be sent automatically when someone mentions your name in chat.
 */
public class StatusMessage extends Module {

    private final StringSetting message = register(new StringSetting(
            "Message", "Status message to broadcast", "I'm using Quark.cc!"));

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Seconds between auto-broadcasts (0 = manual only)", 0, 0, 600));

    private final BoolSetting replyOnMention = register(new BoolSetting(
            "Reply On Mention", "Auto-send status when your name is mentioned", false));

    private final BoolSetting useCommand = register(new BoolSetting(
            "Use Command", "Send as /status <message> instead of plain chat", false));

    private final StringSetting commandFormat = register(new StringSetting(
            "Command", "Command format, {msg} replaced with message", "/status {msg}"));

    private final TimerUtil broadcastTimer = new TimerUtil();
    private final TimerUtil mentionCooldown = new TimerUtil();

    public StatusMessage() {
        super("StatusMessage", "Sets a custom status message shown in chat periodically", Category.MISC);
    }

    @Override
    public void onEnable() {
        broadcastTimer.reset();
        mentionCooldown.reset();
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        int secs = interval.get();
        if (secs <= 0) return;
        if (broadcastTimer.hasReached(secs * 1000L)) {
            sendStatus();
            broadcastTimer.reset();
        }
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        if (!replyOnMention.isEnabled()) return;
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!mentionCooldown.hasReached(10000)) return; // 10-second cooldown per mention

        String msg = event.getMessage();
        if (msg == null) return;
        String name = mc.player.getName().getString().toLowerCase();
        if (msg.toLowerCase().contains(name)) {
            sendStatus();
            mentionCooldown.reset();
        }
    }

    private void sendStatus() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        String text = message.get().trim();
        if (text.isEmpty()) return;

        if (useCommand.isEnabled()) {
            String cmd = commandFormat.get().replace("{msg}", text);
            if (cmd.startsWith("/")) {
                mc.getNetworkHandler().sendCommand(cmd.substring(1));
            } else {
                mc.getNetworkHandler().sendChatMessage(cmd);
            }
        } else {
            mc.getNetworkHandler().sendChatMessage(text);
        }
    }
}
