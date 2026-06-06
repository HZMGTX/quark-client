package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;

public class NameProtect extends Module {

    private final StringSetting fakeName = register(new StringSetting(
            "FakeName", "Name shown instead of your real username", "Player"));

    private String realName = null;

    public NameProtect() {
        super("NameProtect", "Replaces your own username in chat and nametags with a custom name", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            realName = mc.player.getGameProfile().getName();
        }
    }

    @Override
    public void onDisable() {
        realName = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        // Refresh real name in case it wasn't available at enable time
        if (realName == null && mc.player != null) {
            realName = mc.player.getGameProfile().getName();
        }
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (realName == null || realName.isEmpty()) return;
        String msg = event.getMessage();
        if (msg.contains(realName)) {
            event.setMessage(msg.replace(realName, fakeName.get()));
        }
    }

    /**
     * Returns the fake display name for use in nametag/chat mixins.
     */
    public String getFakeName() {
        return fakeName.get();
    }

    /**
     * Returns the player's real username, or null when the module is off.
     */
    public String getRealName() {
        return realName;
    }
}
