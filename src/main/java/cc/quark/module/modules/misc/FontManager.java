package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;

public class FontManager extends Module {

    private final ModeSetting font = register(new ModeSetting(
            "Font", "Font renderer mode for HUD elements", "Default", "Default", "Smooth", "Pixel"));

    public FontManager() {
        super("FontManager", "Overrides game font with a custom renderer", Category.MISC);
    }

    public String getFont() {
        return font.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
    }
}
