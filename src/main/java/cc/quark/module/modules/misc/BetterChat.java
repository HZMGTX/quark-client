package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BetterChat extends Module {

    private final BoolSetting timestamps    = register(new BoolSetting("Timestamps",        "Prepend [HH:mm] to incoming messages",              true));
    private final BoolSetting antiSpam      = register(new BoolSetting("Anti Spam",         "Suppress duplicate messages within 2 seconds",      true));
    private final BoolSetting colorMentions = register(new BoolSetting("Colored Mentions",  "Highlight your own username in yellow",              true));
    private final BoolSetting urlHighlight  = register(new BoolSetting("URL Highlight",     "Highlight URLs in cyan",                            true));
    private final BoolSetting filterBadWords= register(new BoolSetting("Filter Bad Words",  "Replace common profanity with ***",                 false));

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");

    private static final List<String> BAD_WORDS = Arrays.asList(
        "fuck", "shit", "ass", "bitch", "cunt", "dick", "cock", "pussy", "whore",
        "nigger", "nigga", "faggot", "retard"
    );

    private String lastMessage = null;
    private long lastMessageTime = 0L;

    public BetterChat() {
        super("BetterChat", "Enhances the chat with timestamps, spam filter, and more", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;

        String msg = event.getMessage();

        if (antiSpam.isEnabled()) {
            long now = System.currentTimeMillis();
            if (msg.equals(lastMessage) && (now - lastMessageTime) < 2000L) {
                event.cancel();
                return;
            }
            lastMessage = msg;
            lastMessageTime = now;
        }

        if (filterBadWords.isEnabled()) {
            msg = applyBadWordFilter(msg);
        }

        if (colorMentions.isEnabled() && mc.player != null) {
            String name = mc.player.getName().getString();
            if (!name.isEmpty() && msg.toLowerCase().contains(name.toLowerCase())) {
                msg = colorize(msg, name, "§e", "§r");
            }
        }

        if (urlHighlight.isEnabled()) {
            msg = highlightUrls(msg);
        }

        if (timestamps.isEnabled()) {
            String time = LocalTime.now().format(TIME_FMT);
            msg = "§7[" + time + "]§r " + msg;
        }

        event.setMessage(msg);
    }

    private String applyBadWordFilter(String msg) {
        String lower = msg.toLowerCase();
        for (String word : BAD_WORDS) {
            int idx = lower.indexOf(word);
            while (idx != -1) {
                boolean wordStart = idx == 0 || !Character.isLetterOrDigit(lower.charAt(idx - 1));
                boolean wordEnd   = idx + word.length() >= lower.length()
                        || !Character.isLetterOrDigit(lower.charAt(idx + word.length()));
                if (wordStart && wordEnd) {
                    msg = msg.substring(0, idx) + "***" + msg.substring(idx + word.length());
                    lower = msg.toLowerCase();
                    idx = lower.indexOf(word, idx + 3);
                } else {
                    idx = lower.indexOf(word, idx + 1);
                }
            }
        }
        return msg;
    }

    private String colorize(String msg, String target, String colorCode, String reset) {
        int idx = msg.toLowerCase().indexOf(target.toLowerCase());
        if (idx == -1) return msg;
        return msg.substring(0, idx) + colorCode + msg.substring(idx, idx + target.length()) + reset + msg.substring(idx + target.length());
    }

    private String highlightUrls(String msg) {
        Matcher m = URL_PATTERN.matcher(msg);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "§b" + Matcher.quoteReplacement(m.group()) + "§r");
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
