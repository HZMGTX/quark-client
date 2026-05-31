package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ChatCleaner extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Max times the same message is shown before suppressing", 2, 1, 10));
    private final BoolSetting filterAds = register(new BoolSetting(
            "FilterAds", "Filter messages containing common ad patterns (discord, store, buy)", false));

    private final Map<String, Integer> messageCounts = new HashMap<>();
    private static final Pattern AD_PATTERN = Pattern.compile(
            "(?i)(discord\\.gg|store\\.|buy\\s|\\$\\d|\\bshop\\b|\\bjoin\\b.*\\.\\w{2,3})");

    public ChatCleaner() {
        super("ChatCleaner", "Removes duplicate and spam chat messages", Category.MISC);
    }

    @Override
    public void onDisable() {
        messageCounts.clear();
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();

        if (filterAds.isEnabled() && AD_PATTERN.matcher(msg).find()) {
            event.cancel();
            return;
        }

        String stripped = msg.replaceAll("§.", "").trim();
        int count = messageCounts.merge(stripped, 1, Integer::sum);
        if (count > threshold.get()) {
            event.cancel();
        }
    }
}
