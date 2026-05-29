package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

import java.util.ArrayList;
import java.util.List;

public class ChatFilter extends Module {

    private final BoolSetting filterAds   = register(new BoolSetting("Filter Ads",   "Block messages containing store/shop/buy links", true));
    private final BoolSetting filterCaps  = register(new BoolSetting("Filter Caps",  "Block messages that are all-uppercase", false));
    private final BoolSetting filterSpam  = register(new BoolSetting("Filter Spam",  "Block repeated identical messages", true));

    // Patterns for ad detection
    private static final List<String> AD_PATTERNS = List.of(
            ".store", ".net/", ".com/", "buy now", "shop at", "www.", "http://", "https://", "discord.gg"
    );

    private final List<String> recentMessages = new ArrayList<>();
    private static final int MAX_RECENT = 10;

    public ChatFilter() {
        super("ChatFilter", "Filters ads, caps-spam and repeated messages from incoming chat", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        recentMessages.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        String lower = msg.toLowerCase();

        // Filter advertisements
        if (filterAds.isEnabled()) {
            for (String pattern : AD_PATTERNS) {
                if (lower.contains(pattern)) {
                    event.cancel();
                    return;
                }
            }
        }

        // Filter all-caps messages (at least 10 chars to avoid false positives)
        if (filterCaps.isEnabled() && msg.length() >= 10) {
            String letters = msg.replaceAll("[^a-zA-Z]", "");
            if (!letters.isEmpty() && letters.equals(letters.toUpperCase())) {
                event.cancel();
                return;
            }
        }

        // Filter duplicate/spam messages
        if (filterSpam.isEnabled()) {
            if (recentMessages.contains(msg)) {
                event.cancel();
                return;
            }
            recentMessages.add(msg);
            if (recentMessages.size() > MAX_RECENT) {
                recentMessages.remove(0);
            }
        }
    }
}
