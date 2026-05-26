package cc.quark.module.modules.player;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;

/**
 * NoChatDelay - placeholder toggle that removes the artificial chat send delay.
 */
public class NoChatDelay extends Module {

    public NoChatDelay() {
        super("NoChatDelay", "Removes the client-side chat cooldown", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        ChatUtil.info("Chat delay removed");
    }
}
