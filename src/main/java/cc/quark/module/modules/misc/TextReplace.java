package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class TextReplace extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How to match text",
            "Contains", "Contains", "StartsWith", "Regex"));

    // Using ModeSetting as string holders for find/replace values
    private final ModeSetting find    = register(new ModeSetting("Find",    "Text to search for",      "example",  "example",  "hello", "gg", "bruh", "lol"));
    private final ModeSetting replace = register(new ModeSetting("Replace", "Text to replace with",    "replaced", "replaced", "hi",   "gg", "lol",  "haha"));

    public TextReplace() {
        super("TextReplace", "Replaces text in incoming chat messages based on configured find/replace rules", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;

        String msg = event.getMessage();
        String findStr = find.get();
        String replaceStr = replace.get();

        if (findStr.isEmpty()) return;

        String result = switch (mode.get()) {
            case "Contains"   -> msg.contains(findStr) ? msg.replace(findStr, replaceStr) : msg;
            case "StartsWith" -> msg.startsWith(findStr) ? replaceStr + msg.substring(findStr.length()) : msg;
            case "Regex"      -> applyRegex(msg, findStr, replaceStr);
            default -> msg;
        };

        if (!result.equals(msg)) event.setMessage(result);
    }

    private String applyRegex(String msg, String pattern, String replacement) {
        try {
            return Pattern.compile(pattern).matcher(msg).replaceAll(replacement);
        } catch (PatternSyntaxException e) {
            return msg;
        }
    }
}
