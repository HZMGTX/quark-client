package cc.quark.module.modules.world;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

public class BlockRotator extends Module {

    private final BoolSetting auto = register(new BoolSetting("Auto", "Rotate blocks automatically on place", true));
    private final ModeSetting mode = register(new ModeSetting("Mode", "Rotation mode", "Face", "Face", "Grid"));

    public BlockRotator() {
        super("BlockRotator", "Rotates placed blocks to best orientation", Category.WORLD);
    }
}
