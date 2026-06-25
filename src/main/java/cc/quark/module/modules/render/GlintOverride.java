package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;

/**
 * GlintOverride - overrides the enchantment glint color applied to items.
 * The actual color injection is done via a mixin on ItemRenderer; this module
 * exposes the settings and provides a static accessor used by the mixin.
 */
public class GlintOverride extends Module {

    private static GlintOverride instance;

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Custom enchantment glint color (ARGB)", 0xFF00AAFF));

    private final BoolSetting always = register(new BoolSetting(
            "Always", "Apply glint override to all items, not just enchanted ones", false));

    public GlintOverride() {
        super("GlintOverride", "Overrides item enchantment glint color", Category.RENDER);
        instance = this;
    }

    public static GlintOverride getInstance() {
        return instance;
    }

    /** Returns the ARGB color to use for the glint, or -1 if module is disabled. */
    public static int getGlintColor() {
        if (instance == null || !instance.isEnabled()) return -1;
        return instance.color.get();
    }

    public static boolean isAlways() {
        return instance != null && instance.isEnabled() && instance.always.isEnabled();
    }

    @EventHandler
    public void onTick(EventTick event) {
        // No per-tick logic needed; color is read statically by mixin
    }
}
