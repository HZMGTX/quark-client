package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class ChatFilter2 extends Module {
    private final StringSetting keywords = register(new StringSetting("Keywords", "Comma-separated words to flag", "cheat,hack,xray"));
    private final BoolSetting blockMessage = register(new BoolSetting("BlockMessage", "Block flagged messages", false));
    private final BoolSetting alertStaff = register(new BoolSetting("AlertStaff", "Alert staff of filtered messages", true));
    public ChatFilter2() { super("ChatFilter2", "Filters and flags suspicious chat messages", Category.STAFF); }
    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage().toLowerCase();
        for (String kw : keywords.getValue().split(",")) {
            if (msg.contains(kw.trim())) {
                if (alertStaff.getValue()) ChatUtil.warn("[ChatFilter] Flagged: " + event.getMessage());
                if (blockMessage.getValue()) event.cancel();
                return;
            }
        }
    }
}
