package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;

public class NoBob extends Module {

    public NoBob() {
        super("NoBob", "Disables the view bobbing animation", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            mc.options.getBobView().setValue(false);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getBobView().setValue(true);
        }
    }
}
