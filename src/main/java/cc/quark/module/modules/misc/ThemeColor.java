package cc.quark.module.modules.misc;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;

public class ThemeColor extends Module {

    private final ColorSetting primary = register(new ColorSetting(
            "Primary", "Primary HUD color", 0xFF00AAFF));
    private final ColorSetting accent  = register(new ColorSetting(
            "Accent",  "Accent HUD color",  0xFF7700FF));

    public ThemeColor() {
        super("ThemeColor", "Global color theme override for all HUD elements", Category.MISC);
    }

    public int getPrimary() { return primary.get(); }
    public int getAccent()  { return accent.get(); }

    public static ThemeColor getInstance() {
        try {
            return cc.quark.Quark.getInstance().getModuleManager()
                    .getModule(ThemeColor.class);
        } catch (Exception e) {
            return null;
        }
    }
}
