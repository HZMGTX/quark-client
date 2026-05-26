package cc.quark.module.modules.world;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class AutoSmelter extends Module {

    private final BoolSetting autoTransfer = register(new BoolSetting("Auto Transfer", "Auto-transfer mined ores to furnace", true));

    public AutoSmelter() {
        super("AutoSmelter", "Auto-transfers mined ores to furnace (stub - requires world interaction mixin)", Category.WORLD);
    }
}
