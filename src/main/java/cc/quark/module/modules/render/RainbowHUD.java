package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;

/**
 * RainbowHUD - exposes a static helper so other HUD elements can query the
 * current rainbow color when this module is enabled.  No direct rendering
 * here; modules that want rainbow coloring check {@link #getColor(float)}.
 */
public class RainbowHUD extends Module {

    private static RainbowHUD instance;

    public RainbowHUD() {
        super("RainbowHUD", "Makes HUD elements cycle through rainbow colors when enabled", Category.RENDER);
        instance = this;
    }

    /**
     * Returns the current rainbow ARGB color shifted by {@code offset} hue.
     * Returns white (0xFFFFFFFF) when the module is disabled so callers can
     * unconditionally use this value.
     */
    public static int getColor(float offset) {
        if (instance != null && instance.isEnabled()) {
            return RenderUtil.rainbowColor(offset);
        }
        return 0xFFFFFFFF;
    }
}
