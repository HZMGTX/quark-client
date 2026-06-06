package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

/**
 * AntiChatFilter - Bypasses server-side chat filters by substituting visually
 * identical Unicode characters for filtered ASCII letters.
 *
 * When Mode is "Unicode" each alphabetical character is replaced with a
 * look-alike from the Unicode Fullwidth or Latin Extended blocks so the message
 * passes naive keyword checks but still reads normally to humans.
 *
 * When Mode is "Zalgo" invisible combining characters are inserted between
 * letters to break exact-match filters without affecting readability at a glance.
 *
 * NOTE: this module intercepts *outgoing* chat events and rewrites the message
 * before it is sent.  It does not affect incoming messages.
 */
public class AntiChatFilter extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Substitution strategy",
            "Unicode", "Unicode", "Zalgo", "Invisible"));

    private final BoolSetting onlyCommands = register(new BoolSetting(
            "Only Commands", "Only rewrite messages starting with '/'", false));

    public AntiChatFilter() {
        super("AntiChatFilter", "Bypasses server chat filters with unicode substitution", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming()) return;                       // outgoing only
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        String original = event.getMessage();
        if (original == null || original.isEmpty()) return;
        if (onlyCommands.isEnabled() && !original.startsWith("/")) return;

        String rewritten = switch (mode.get()) {
            case "Unicode"   -> unicodeSubstitute(original);
            case "Zalgo"     -> zalgoInsert(original);
            case "Invisible" -> invisibleInsert(original);
            default          -> original;
        };

        if (!rewritten.equals(original)) {
            event.cancel();
            // Re-send the rewritten message on the next tick to avoid re-triggering this handler.
            String toSend = rewritten;
            if (toSend.startsWith("/")) {
                mc.getNetworkHandler().sendCommand(toSend.substring(1));
            } else {
                mc.getNetworkHandler().sendChatMessage(toSend);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Substitution strategies
    // -------------------------------------------------------------------------

    /** Replace each ASCII letter with its Unicode fullwidth equivalent. */
    private static String unicodeSubstitute(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                // Fullwidth lowercase: a=FF41, b=FF42 ...
                sb.append((char)('ａ' + (c - 'a')));
            } else if (c >= 'A' && c <= 'Z') {
                // Fullwidth uppercase: A=FF21
                sb.append((char)('Ａ' + (c - 'A')));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Insert a Combining Grapheme Joiner (U+034F) between each character. */
    private static String zalgoInsert(String text) {
        StringBuilder sb = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));
            if (i < text.length() - 1) {
                sb.append('͏'); // CGJ - invisible combining character
            }
        }
        return sb.toString();
    }

    /** Insert zero-width non-joiner (U+200C) between each character. */
    private static String invisibleInsert(String text) {
        StringBuilder sb = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            sb.append(text.charAt(i));
            if (i < text.length() - 1) {
                sb.append('‌'); // ZWNJ
            }
        }
        return sb.toString();
    }
}
