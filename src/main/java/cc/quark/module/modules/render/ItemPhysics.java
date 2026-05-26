package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class ItemPhysics extends Module {

    private final BoolSetting randomRotation = register(new BoolSetting("Random Rotation", "Give dropped items random initial rotation", true));

    public ItemPhysics() {
        super("ItemPhysics", "Shows dropped items with physics-style rotation (requires mixin)", Category.RENDER);
    }
}
