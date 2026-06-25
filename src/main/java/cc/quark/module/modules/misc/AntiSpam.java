package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

import java.util.ArrayDeque;
import java.util.Deque;

public class AntiSpam extends Module {

    private final IntSetting maxDuplicates = register(new IntSetting(
            "Max Duplicates", "Max repeated messages before filtering", 2, 1, 10));
    private final IntSetting historySize = register(new IntSetting(
            "History", "Recent messages to compare against", 5, 2, 20));
    private final BoolSetting filterCaps = register(new BoolSetting(
            "Filter Caps", "Cancel all-caps spam messages", true));
    private final BoolSetting filterSymbols = register(new BoolSetting(
            "Filter Symbols", "Cancel symbol-heavy messages", true));

    private final Deque<String> recentMessages = new ArrayDeque<>();

    public AntiSpam() {
        super("AntiSpam", "Filters repetitive and spam chat messages", Category.MISC);
    }

    @Override
    public void onEnable() {
        recentMessages.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;

        String msg = event.getMessage();
        if (msg == null) return;

        String clean = msg.replaceAll("§[0-9a-fklmnor]", "").trim();

        // All-caps filter
        if (filterCaps.isEnabled() && clean.length() > 8) {
            long upper = clean.chars().filter(Character::isUpperCase).count();
            long letter = clean.chars().filter(Character::isLetter).count();
            if (letter > 0 && (double) upper / letter > 0.85) {
                event.cancel();
                return;
            }
        }

        // Symbol-heavy filter
        if (filterSymbols.isEnabled() && clean.length() > 8) {
            long symbols = clean.chars().filter(c -> !Character.isLetterOrDigit(c) && c != ' ').count();
            if ((double) symbols / clean.length() > 0.5) {
                event.cancel();
                return;
            }
        }

        // Duplicate filter
        long duplicates = recentMessages.stream()
            .filter(m -> m.equalsIgnoreCase(clean))
            .count();
        if (duplicates >= maxDuplicates.get()) {
            event.cancel();
            return;
        }

        // Add to history
        recentMessages.addFirst(clean);
        while (recentMessages.size() > historySize.get()) {
            recentMessages.removeLast();
        }
    }
}
