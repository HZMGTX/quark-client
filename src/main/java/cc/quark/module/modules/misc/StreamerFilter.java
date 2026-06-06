package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

public class StreamerFilter extends Module {

    private final BoolSetting hideCoords = register(new BoolSetting("HideCoords", "Replace coordinates in chat", true));
    private final BoolSetting hideIP = register(new BoolSetting("HideIP", "Hide server IP in messages", true));
    private final BoolSetting hideNames = register(new BoolSetting("HideNames", "Hide player names", false));
    private final StringSetting replacement = register(new StringSetting("Replacement", "Replacement text for hidden info", "[hidden]"));

    public StreamerFilter() {
        super("StreamerFilter", "Hides sensitive info for streaming", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage();
        if (hideCoords.getValue()) {
            msg = msg.replaceAll("-?\\d+,\\s*-?\\d+,\\s*-?\\d+", replacement.getValue());
            msg = msg.replaceAll("X:\\s*-?\\d+", "X: " + replacement.getValue());
            msg = msg.replaceAll("Z:\\s*-?\\d+", "Z: " + replacement.getValue());
        }
        if (hideIP.getValue()) {
            msg = msg.replaceAll("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", replacement.getValue());
        }
        event.setMessage(msg);
    }
}
