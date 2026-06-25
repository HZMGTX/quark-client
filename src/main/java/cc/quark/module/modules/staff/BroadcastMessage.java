package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class BroadcastMessage extends Module {
    private final StringSetting message = register(new StringSetting("Message", "Message to broadcast", "Server message"));
    private final ModeSetting method = register(new ModeSetting("Method", "Broadcast method", "say", "say", "broadcast", "announce", "alert"));
    private final BoolSetting addPrefix = register(new BoolSetting("Prefix", "Add [STAFF] prefix", true));

    public BroadcastMessage() {
        super("Broadcast", "Send a broadcast message to all players", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String msg = message.get();
        if (addPrefix.isEnabled()) msg = "[STAFF] " + msg;
        mc.player.networkHandler.sendChatCommand(method.get() + " " + msg);
        ChatUtil.info("[Broadcast] Sent: " + msg);
        disable();
    }
}
