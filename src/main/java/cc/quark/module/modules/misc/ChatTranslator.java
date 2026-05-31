package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

public class ChatTranslator extends Module {

    private final BoolSetting autoTranslate = register(new BoolSetting("AutoTranslate", "Automatically show translation hint for non-ASCII messages", true));
    private final BoolSetting onlyIncoming  = register(new BoolSetting("OnlyIncoming",  "Only check incoming messages",                                true));

    public ChatTranslator() {
        super("ChatTranslator", "Detects non-English chat messages and shows a translation hint", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!autoTranslate.isEnabled()) return;
        if (onlyIncoming.isEnabled() && !event.isIncoming()) return;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) return;

        String stripped = msg.replaceAll("§.", "").replaceAll("[\\x00-\\x7F]", "");
        if (!stripped.isEmpty()) {
            ChatUtil.info("§7[Translator] Non-ASCII message detected. Copy and paste into a translator: §f" + msg.replaceAll("§.", ""));
        }
    }
}
