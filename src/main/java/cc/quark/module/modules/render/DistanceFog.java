package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * DistanceFog - controls render-distance fog density.
 * Actual fog suppression is done via MixinBackgroundRenderer reading this module's state.
 */
public class DistanceFog extends Module {

    private static DistanceFog instance;

    private final DoubleSetting density = register(new DoubleSetting(
            "Density", "Fog density multiplier (0.0 = no fog, 1.0 = vanilla)", 0.0, 0.0, 1.0));

    private final BoolSetting disable = register(new BoolSetting(
            "Disable", "Completely disable all distance fog", true));

    public DistanceFog() {
        super("DistanceFog", "Controls render distance fog", Category.RENDER);
        instance = this;
    }

    public static DistanceFog getInstance() { return instance; }

    public static boolean isFogDisabled() {
        return instance != null && instance.isEnabled() && instance.disable.isEnabled();
    }

    public static float getFogDensity() {
        if (instance == null || !instance.isEnabled()) return 1.0f;
        return (float) instance.density.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        // No per-tick logic; fog state is read by mixin
    }
}
