package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class Trajectories extends Module {

    private final BoolSetting arrows = register(new BoolSetting("Arrows", "Show arrow trajectories", true));
    private final BoolSetting potions = register(new BoolSetting("Potions", "Show splash potion trajectories", true));

    public Trajectories() {
        super("Trajectories", "Renders predicted projectile trajectories (stub - requires mixin)", Category.RENDER);
    }
}
