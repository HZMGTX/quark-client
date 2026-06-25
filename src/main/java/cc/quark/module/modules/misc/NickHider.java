package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;

public class NickHider extends Module {

    private final StringSetting fakeNick = register(new StringSetting(
            "Fake Nick", "Replacement name shown in outgoing messages", "Player"));
    private final BoolSetting hideInTab = register(new BoolSetting(
            "Hide In Tab", "Replace real name in tab-list display", true));
    private final BoolSetting hideInChat = register(new BoolSetting(
            "Hide In Chat", "Replace real name in outgoing chat messages", true));

    public NickHider() {
        super("NickHider", "Replaces your username with a fake nick in chat", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (event.isIncoming() || mc.player == null) return;
        if (!hideInChat.isEnabled()) return;

        String realName = mc.player.getName().getString();
        String message  = event.getMessage();
        if (message == null) return;

        // Replace the real name in the outgoing message with the fake nick
        String replaced = message.replace(realName, fakeNick.getValue());
        if (!replaced.equals(message)) {
            event.setMessage(replaced);
        }
    }

    @Override
    public String getSuffix() {
        return fakeNick.getValue();
    }
}
