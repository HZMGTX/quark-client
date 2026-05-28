package cc.quark.module.modules.misc;

import cc.quark.gui.ThemeManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;

public class GuiTheme extends Module {

    private final ModeSetting theme = register(new ModeSetting(
            "Theme", "GUI color theme", "Default",
            "Default", "Catppuccin", "Nord", "One Dark", "Dracula", "Rose Pine"));

    public GuiTheme() {
        super("GuiTheme", "Change the GUI color theme", Category.MISC);
    }

    @Override
    public String getSuffix() {
        applyTheme();
        return theme.get();
    }

    private void applyTheme() {
        ThemeManager.Theme t = switch (theme.get()) {
            case "Catppuccin" -> ThemeManager.Theme.CATPPUCCIN;
            case "Nord"       -> ThemeManager.Theme.NORD;
            case "One Dark"   -> ThemeManager.Theme.ONE_DARK;
            case "Dracula"    -> ThemeManager.Theme.DRACULA;
            case "Rose Pine"  -> ThemeManager.Theme.ROSE_PINE;
            default           -> ThemeManager.Theme.DEFAULT;
        };
        ThemeManager.INSTANCE.setTheme(t);
    }
}
