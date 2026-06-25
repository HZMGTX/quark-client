package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

public class AntiAliasing extends Module {
    private final IntSetting level = register(new IntSetting("Level", "MSAA samples (0=off, 2/4/8)", 4, 0, 8));

    public AntiAliasing() {
        super("Anti Aliasing", "Toggle MSAA anti-aliasing level", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        // Requires restart to take effect; stores preference
    }

    public int getLevel() { return level.get(); }
}
