package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

public class GuiScale extends Module {

    private final IntSetting scale = register(new IntSetting(
            "Scale", "Forced GUI scale (1=small, 2=normal, 3=large, 4=auto/largest)", 2, 1, 4));

    private int savedScale = 0;

    public GuiScale() {
        super("GuiScale", "Forces a custom GUI scale factor", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            savedScale = mc.options.getGuiScale().getValue();
            mc.options.getGuiScale().setValue(scale.get());
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getGuiScale().setValue(savedScale);
        }
    }

    @Override
    public String getSuffix() {
        return "x" + scale.get();
    }
}
