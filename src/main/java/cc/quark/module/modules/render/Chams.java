package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;

public class Chams extends Module {

    private final BoolSetting players = register(new BoolSetting(
            "Players", "Show players through walls", true));

    private final BoolSetting mobs = register(new BoolSetting(
            "Mobs", "Show mobs through walls", false));

    private final IntSetting alpha = register(new IntSetting(
            "Alpha", "Transparency of chams overlay", 150, 50, 255));

    public boolean enabled = true;

    public Chams() {
        super("Chams", "Makes entities visible through walls with colored tint", Category.RENDER);
    }
}
