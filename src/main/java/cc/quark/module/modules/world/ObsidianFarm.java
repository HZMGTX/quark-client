package cc.quark.module.modules.world;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;

public class ObsidianFarm extends Module {
    private final IntSetting amount = register(new IntSetting("Amount", "Obsidian blocks to farm", 64, 1, 256));

    public ObsidianFarm() {
        super("Obsidian Farm", "Guides obsidian farming via lava+water", Category.WORLD, 0);
    }

    @Override
    public void onEnable() {
        ChatUtil.info("[ObsidianFarm] Place lava first, then right-click with a water bucket over it.");
        ChatUtil.info("[ObsidianFarm] Target: §f" + amount.get() + " §7blocks. Use with AutoBreaker.");
        disable();
    }
}
