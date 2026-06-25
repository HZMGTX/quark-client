package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;

/**
 * ItemGlint - Controls the enchantment glint rendered on items.
 *
 * Modes:
 *   Disable  - removes the glint entirely
 *   Rainbow  - cycles through hues over time
 *   Custom   - uses a user-picked ARGB color
 *   Default  - vanilla behavior (module does nothing extra)
 *
 * A static accessor is read by the ItemRenderer mixin to determine the glint
 * color (or whether to skip it) without a full module-registry lookup.
 */
public class ItemGlint extends Module {

    private static ItemGlint instance;

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Glint rendering mode",
            "Disable", "Disable", "Rainbow", "Custom", "Default"));

    private final ColorSetting customColor = register(new ColorSetting(
            "Custom Color", "ARGB color used in Custom mode", 0xFF00AAFF));

    private final BoolSetting applyToAll = register(new BoolSetting(
            "Apply To All", "Apply glint to non-enchanted items too (Custom/Rainbow only)", false));

    public ItemGlint() {
        super("ItemGlint", "Controls enchantment glint: disable, rainbow, or custom color", Category.RENDER);
        instance = this;
    }

    public static ItemGlint getInstance() { return instance; }

    /**
     * Returns the ARGB color the glint should use, or -1 to skip rendering.
     * Returns 0 to use vanilla behavior.
     */
    public static int getGlintColor() {
        if (instance == null || !instance.isEnabled()) return 0; // vanilla
        return switch (instance.mode.get()) {
            case "Disable" -> -1;
            case "Rainbow" -> RenderUtil.rainbowColor(0f);
            case "Custom"  -> instance.customColor.get();
            default        -> 0; // Default mode
        };
    }

    public static boolean shouldApplyToAll() {
        return instance != null && instance.isEnabled() && instance.applyToAll.isEnabled();
    }

    @EventHandler
    public void onTick(EventTick e) {
        // Settings consumed statically by mixin.
    }
}
