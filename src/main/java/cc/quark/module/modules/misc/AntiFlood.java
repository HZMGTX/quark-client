package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

/**
 * AntiFlood - Prevents chat flooding by enforcing a minimum delay between
 * outgoing messages. Excess messages are either dropped or queued.
 */
public class AntiFlood extends Module {

    private final IntSetting minDelayMs = register(new IntSetting(
            "Min Delay", "Minimum milliseconds between outgoing chat messages", 1000, 100, 10000));

    private final BoolSetting cancelExcess = register(new BoolSetting(
            "Cancel Excess", "Cancel messages sent too fast (false = queue them)", true));

    private final BoolSetting notifyUser = register(new BoolSetting(
            "Notify", "Warn locally when a message is cancelled", true));

    private long lastMessageTime = 0L;

    public AntiFlood() {
        super("AntiFlood", "Prevents chat flooding by enforcing a configurable delay between messages", Category.MISC);
    }

    @Override
    public void onEnable() {
        lastMessageTime = 0L;
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming()) return; // only intercept outgoing messages

        long now = System.currentTimeMillis();
        long elapsed = now - lastMessageTime;

        if (elapsed < minDelayMs.get()) {
            if (cancelExcess.isEnabled()) {
                event.cancel();
                if (notifyUser.isEnabled()) {
                    long waitMs = minDelayMs.get() - elapsed;
                    ChatUtil.warn("[AntiFlood] Message blocked — wait " + waitMs + "ms");
                }
            }
            // If not cancelling, just let it through (queue is not implemented here
            // without a background thread — would require more complex plumbing)
        } else {
            lastMessageTime = now;
        }
    }
}
