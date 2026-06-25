package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class NickChanger extends Module {
    private final StringSetting nick = register(new StringSetting("Nickname", "Your new nickname", "Steve"));
    private final StringSetting command = register(new StringSetting("Command", "Nick command", "nick"));
    private boolean set = false;

    public NickChanger() { super("NickChanger", "Changes your server nickname via command", Category.STAFF); }
    @Override public void onEnable() { set = false; }
    @Override public void onDisable() {
        if (mc.player != null && set) mc.player.networkHandler.sendChatCommand(command.get() + " off");
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || set) return;
        mc.player.networkHandler.sendChatCommand(command.get() + " " + nick.get());
        ChatUtil.info("Nick set to: " + nick.get());
        set = true;
    }
}
