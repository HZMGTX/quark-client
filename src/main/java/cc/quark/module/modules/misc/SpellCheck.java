package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

import java.util.Set;

public class SpellCheck extends Module {

    private final BoolSetting autoCorrect = register(new BoolSetting("AutoCorrect", "Attempt to auto-correct common misspellings before sending", false));

    private static final Set<String> COMMON_WORDS = Set.of(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "it",
            "for", "not", "on", "with", "he", "as", "you", "do", "at", "this",
            "but", "his", "by", "from", "they", "we", "say", "her", "she", "or",
            "an", "will", "my", "one", "all", "would", "there", "their", "what",
            "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
            "when", "make", "can", "like", "time", "no", "just", "him", "know",
            "take", "people", "into", "year", "your", "good", "some", "could",
            "them", "see", "other", "than", "then", "now", "look", "only", "come",
            "its", "over", "think", "also", "back", "after", "use", "two", "how",
            "our", "well", "way", "even", "new", "want", "because", "any", "these",
            "give", "day", "most", "us", "player", "server", "game", "kill", "join"
    );

    public SpellCheck() {
        super("SpellCheck", "Highlights likely misspelled words in outgoing chat messages", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg == null || msg.startsWith("/")) return;

        StringBuilder issues = new StringBuilder();
        String[] words = msg.split("\\s+");
        for (String word : words) {
            String clean = word.toLowerCase().replaceAll("[^a-z]", "");
            if (clean.length() < 4) continue;
            if (!COMMON_WORDS.contains(clean) && looksLikeTypo(clean)) {
                if (issues.length() > 0) issues.append(", ");
                issues.append(word);
            }
        }

        if (issues.length() > 0) {
            ChatUtil.warn("[SpellCheck] Possibly misspelled: " + issues);
        }
    }

    private boolean looksLikeTypo(String word) {
        // Heuristic: repeated consecutive letters beyond 2, or very long words with no vowels
        int vowels = 0;
        for (char c : word.toCharArray()) {
            if ("aeiou".indexOf(c) >= 0) vowels++;
        }
        if (word.length() > 4 && vowels == 0) return true;
        for (int i = 0; i < word.length() - 2; i++) {
            if (word.charAt(i) == word.charAt(i + 1) && word.charAt(i + 1) == word.charAt(i + 2)) return true;
        }
        return false;
    }
}
