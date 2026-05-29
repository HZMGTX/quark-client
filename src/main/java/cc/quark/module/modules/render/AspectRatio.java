package cc.quark.module.modules.render;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * AspectRatio — forces a custom aspect ratio for rendering.
 *
 * The actual override is applied in a mixin that calls {@link #getAspectRatio()}
 * during the projection matrix setup whenever this module is enabled.
 * Stored/restored via onEnable/onDisable (the mixin falls back to the real
 * window ratio when the module is off).
 */
public class AspectRatio extends Module {

    public static AspectRatio INSTANCE;

    private final DoubleSetting width  = register(new DoubleSetting(
            "Width",  "Numerator of the forced aspect ratio",  16.0, 1.0, 32.0));
    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Denominator of the forced aspect ratio", 9.0, 1.0, 32.0));

    public AspectRatio() {
        super("AspectRatio", "Forces a custom aspect ratio for the game viewport", Category.RENDER);
        INSTANCE = this;
    }

    /**
     * Returns the forced aspect ratio (width / height).
     * Called from the mixin during projection matrix calculation.
     */
    public float getAspectRatio() {
        double h = height.get();
        if (h == 0) return 16f / 9f;
        return (float) (width.get() / h);
    }

    @Override
    public String getSuffix() {
        return String.format("%.0f:%.0f", width.get(), height.get());
    }
}
