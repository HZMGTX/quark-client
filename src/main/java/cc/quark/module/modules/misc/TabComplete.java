package cc.quark.module.modules.misc;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class TabComplete extends Module {
    private final BoolSetting playerNames = register(new BoolSetting("Player Names", "Tab-complete player names", true));
    private final BoolSetting commands = register(new BoolSetting("Commands", "Tab-complete Quark commands", true));

    public TabComplete() {
        super("Tab Complete", "Enhanced chat tab completion", Category.MISC, 0);
    }
}
