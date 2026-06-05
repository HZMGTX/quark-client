package cc.quark.module.modules.world;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;

public class CactusGrinder extends Module {
    public CactusGrinder() {
        super("Cactus Grinder", "Setup guide for cactus mob grinder", Category.WORLD, 0);
    }

    @Override
    public void onEnable() {
        ChatUtil.info("[CactusGrinder] Build a 1-wide path with cactus on sides.");
        ChatUtil.info("[CactusGrinder] Mobs pushed into cactus die. Use with AutoPickup.");
        disable();
    }
}
