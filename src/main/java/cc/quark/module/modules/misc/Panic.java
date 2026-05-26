package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

// Disables all active modules instantly (panic button — useful to look legit fast)
public class Panic extends Module {

    private final BoolSetting keepHUD = register(new BoolSetting(
            "Keep HUD", "Keep HUD enabled after panic", true));

    public Panic() {
        super("Panic", "Instantly disables all active modules", Category.MISC);
    }

    @Override
    public void onEnable() {
        Module.silent = true; // suppress notifications for the mass-disable
        var mm = Quark.getInstance().getModuleManager();
        for (Module m : mm.getModules()) {
            if (m == this) continue;
            if (keepHUD.isEnabled() && m.getName().equals("HUD")) continue;
            if (m.isEnabled()) m.disable();
        }
        Module.silent = false;
        // disable self so it acts as a one-shot trigger
        this.disable();
    }
}
