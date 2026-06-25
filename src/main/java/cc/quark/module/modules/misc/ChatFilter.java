package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

import java.util.Arrays;
import java.util.List;

public class ChatFilter extends Module {

    private final BoolSetting   whitelist  = register(new BoolSetting  ("Whitelist Mode", "Only show messages containing whitelist words", false));
    private final StringSetting blacklist  = register(new StringSetting("Blacklist",       "Comma-separated words to hide",                ""));
    private final StringSetting whitelistW = register(new StringSetting("Whitelist Words", "Comma-separated words to allow (whitelist mode)", ""));
    private final BoolSetting   hideJoin   = register(new BoolSetting  ("Hide Join/Leave", "Hide join and leave messages",                  false));
    private final BoolSetting   hideAdvance= register(new BoolSetting  ("Hide Advancements","Hide advancement messages",                    false));
    private final BoolSetting   hideDeaths = register(new BoolSetting  ("Hide Deaths",      "Hide death messages",                          false));

    private static final List<String> JOIN_LEAVE_PATTERNS = Arrays.asList(
        "joined the game", "left the game", "has left", "has joined"
    );
    private static final List<String> DEATH_PATTERNS = Arrays.asList(
        "was slain", "drowned", "blew up", "hit the ground", "fell from",
        "was shot", "was killed", "burned to death", "starved to death"
    );

    public ChatFilter() {
        super("ChatFilter", "Filter unwanted chat messages with blacklist/whitelist", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        String msg = event.getMessage().toLowerCase();

        if (hideJoin.isEnabled() && JOIN_LEAVE_PATTERNS.stream().anyMatch(msg::contains)) {
            event.cancel();
            return;
        }
        if (hideDeaths.isEnabled() && DEATH_PATTERNS.stream().anyMatch(msg::contains)) {
            event.cancel();
            return;
        }
        if (hideAdvance.isEnabled() && (msg.contains("has made the advancement") || msg.contains("has completed the challenge"))) {
            event.cancel();
            return;
        }

        if (whitelist.isEnabled()) {
            String wl = whitelistW.get().trim();
            if (!wl.isEmpty()) {
                boolean pass = Arrays.stream(wl.split(","))
                        .map(String::trim).filter(s -> !s.isEmpty())
                        .anyMatch(msg::contains);
                if (!pass) { event.cancel(); return; }
            }
        }

        String bl = blacklist.get().trim();
        if (!bl.isEmpty()) {
            for (String word : bl.split(",")) {
                word = word.trim().toLowerCase();
                if (!word.isEmpty() && msg.contains(word)) {
                    event.cancel();
                    return;
                }
            }
        }
    }
}
