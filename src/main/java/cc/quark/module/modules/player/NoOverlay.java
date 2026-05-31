package cc.quark.module.modules.player;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class NoOverlay extends Module {

    private final BoolSetting pumpkin = register(new BoolSetting("Pumpkin", "Remove pumpkin head overlay", true));
    private final BoolSetting fire    = register(new BoolSetting("Fire",    "Remove fire overlay when burning", true));
    private final BoolSetting water   = register(new BoolSetting("Water",   "Remove underwater vision overlay", true));

    public NoOverlay() {
        super("NoOverlay", "Removes pumpkin/shield/fire/water overlays from the screen", Category.PLAYER);
    }

    public boolean noPumpkin() { return isEnabled() && pumpkin.isEnabled(); }
    public boolean noFire()    { return isEnabled() && fire.isEnabled(); }
    public boolean noWater()   { return isEnabled() && water.isEnabled(); }
}
