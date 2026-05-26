package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

/**
 * AutoGG - sends "gg" at the end of a game (triggered manually here).
 */
public class AutoGG extends Module {

    private final BoolSetting glhf = register(new BoolSetting("GLHF", "Also send glhf on enable", true));

    public AutoGG() {
        super("AutoGG", "Automatically says gg", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        if (glhf.isEnabled()) ChatUtil.send("glhf");
    }
}
