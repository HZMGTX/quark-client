package cc.quark.module.modules.world;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;

public class BaseBuilder extends Module {
    private final ModeSetting style = register(new ModeSetting("Style", "Base style to build", "Cube", "Cube", "Bunker", "Tower", "Underground"));
    private final IntSetting size = register(new IntSetting("Size", "Base size in blocks", 7, 3, 20));

    public BaseBuilder() {
        super("Base Builder", "Guides automatic base construction", Category.WORLD, 0);
    }

    @Override
    public void onEnable() {
        ChatUtil.info("[BaseBuilder] Style: §f" + style.get() + " §7Size: §f" + size.get() + "x" + size.get());
        ChatUtil.info("[BaseBuilder] Stand at your desired base corner and toggle.");
        ChatUtil.info("[BaseBuilder] Use with AutoBuild module for actual placement.");
        disable();
    }
}
