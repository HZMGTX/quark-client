package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AutoModerate - Monitors incoming chat for banned words/patterns and
 * automatically issues configurable moderation commands against offenders.
 */
public class AutoModerate extends Module {

    private final StringSetting bannedWords = register(new StringSetting(
            "Banned Words", "Comma-separated words/phrases to flag (case-insensitive)", "badword1,badword2"));

    private final BoolSetting muteOffender = register(new BoolSetting(
            "Auto Mute", "Send /mute <player> when triggered", false));

    private final IntSetting muteDuration = register(new IntSetting(
            "Mute Duration (min)", "Duration of auto-mute in minutes", 10, 1, 1440));

    private final BoolSetting kickOffender = register(new BoolSetting(
            "Auto Kick", "Send /kick <player> when triggered", false));

    private final BoolSetting warnOffender = register(new BoolSetting(
            "Auto Warn", "Send /warn <player> when triggered", true));

    private final BoolSetting alertSelf = register(new BoolSetting(
            "Alert Self", "Print alert to your local chat", true));

    private final BoolSetting alertToStaffChat = register(new BoolSetting(
            "Staff Chat Alert", "Forward alert to /staffchat", false));

    private final BoolSetting detectCaps = register(new BoolSetting(
            "Detect Caps", "Flag messages that are mostly uppercase", false));

    private final IntSetting capsThreshold = register(new IntSetting(
            "Caps Threshold %", "Minimum uppercase percentage to flag", 70, 50, 100));

    // Simple <player>: <message> format used by many servers
    private static final Pattern CHAT_PATTERN = Pattern.compile("^<?([A-Za-z0-9_]{2,16})>?:?\\s+(.+)$");

    public AutoModerate() {
        super("AutoModerate", "Auto-moderates chat based on configurable banned words and rules", Category.STAFF, 0);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;

        String raw = event.getMessage();
        if (raw == null || raw.isEmpty()) return;

        Matcher m = CHAT_PATTERN.matcher(raw);
        if (!m.matches()) return;

        String player  = m.group(1);
        String message = m.group(2);

        String reason = detectViolation(message);
        if (reason == null) return;

        if (alertSelf.isEnabled()) {
            ChatUtil.warn("[AutoMod] §c" + player + "§f violated rule: §e" + reason);
        }

        if (alertToStaffChat.isEnabled() && mc.player != null) {
            mc.player.networkHandler.sendChatCommand("staffchat [AutoMod] " + player + " violated: " + reason);
        }

        if (muteOffender.isEnabled() && mc.player != null) {
            mc.player.networkHandler.sendChatCommand("mute " + player + " " + muteDuration.get() + "m " + reason);
        }

        if (kickOffender.isEnabled() && mc.player != null) {
            mc.player.networkHandler.sendChatCommand("kick " + player + " " + reason);
        }

        if (warnOffender.isEnabled() && mc.player != null) {
            mc.player.networkHandler.sendChatCommand("warn " + player + " " + reason);
        }
    }

    private String detectViolation(String message) {
        Set<String> banned = parseBannedWords();
        String lower = message.toLowerCase();

        for (String word : banned) {
            if (!word.isEmpty() && lower.contains(word.toLowerCase())) {
                return "Banned word: " + word;
            }
        }

        if (detectCaps.isEnabled() && message.length() > 5) {
            long uppers = message.chars().filter(Character::isUpperCase).count();
            long letters = message.chars().filter(Character::isLetter).count();
            if (letters > 0 && (uppers * 100 / letters) >= capsThreshold.get()) {
                return "Excessive caps";
            }
        }

        return null;
    }

    private Set<String> parseBannedWords() {
        Set<String> result = new HashSet<>();
        String raw = bannedWords.get().trim();
        if (raw.isEmpty()) return result;
        Arrays.stream(raw.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(result::add);
        return result;
    }
}
