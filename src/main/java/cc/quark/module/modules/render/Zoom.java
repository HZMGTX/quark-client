package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.option.GameOptions;
import org.lwjgl.glfw.GLFW;

/**
 * Zoom - reduces the field of view to simulate a spyglass/zoom effect.
 *
 * While the zoom key (default: C) is held, the FOV is smoothly interpolated
 * toward fovMultiplier * normalFov. Scroll wheel adjusts zoom level.
 * On release the FOV smoothly returns to normal.
 */
public class Zoom extends Module {

    private final DoubleSetting fovMultiplier = register(new DoubleSetting(
            "FOV Multiplier", "Zoom level as a fraction of normal FOV", 0.15, 0.05, 0.9));

    private final BoolSetting smooth = register(new BoolSetting(
            "Smooth", "Smoothly interpolate FOV change", true));

    private final BoolSetting scroll = register(new BoolSetting(
            "Scroll", "Allow scroll wheel to adjust zoom level", true));

    /** The GLFW key that activates zoom (C). */
    private static final int ZOOM_KEY = GLFW.GLFW_KEY_C;

    /** Original FOV saved on first zoom activation. */
    private double savedFov = 70.0;

    /** Current effective FOV multiplier (lerped). */
    private double currentMultiplier = 1.0;

    /** Whether zoom is actively held. */
    private boolean zooming = false;

    /** Dynamic multiplier adjusted by scroll. */
    private double scrollMultiplier = 1.0;

    public Zoom() {
        super("Zoom", "Hold C to zoom in â€” scroll to adjust", Category.RENDER);
    }

    @Override
    public void onEnable() {
        zooming = false;
        currentMultiplier = 1.0;
        scrollMultiplier = 1.0;
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.fov.setValue(savedFov);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.options == null) return;

        boolean keyHeld = GLFW.glfwGetKey(mc.getWindow().getHandle(), ZOOM_KEY) == GLFW.GLFW_PRESS;

        if (keyHeld && !zooming) {
            // Start zoom â€” record current FOV
            savedFov = mc.options.fov.getValue();
            zooming = true;
        } else if (!keyHeld && zooming) {
            // Stop zoom
            zooming = false;
        }

        double targetMultiplier = zooming ? fovMultiplier.get() * scrollMultiplier : 1.0;

        if (smooth.isEnabled()) {
            // Lerp toward target
            currentMultiplier += (targetMultiplier - currentMultiplier) * 0.25;
        } else {
            currentMultiplier = targetMultiplier;
        }

        // Clamp multiplier so we don't get absurd values
        currentMultiplier = Math.max(0.05, Math.min(1.0, currentMultiplier));

        double newFov = savedFov * currentMultiplier;
        mc.options.fov.setValue(newFov);
    }

    /**
     * Called by the mixin when a scroll event is detected.
     * Adjusts the scroll-based multiplier if zoom is active and scroll is enabled.
     *
     * @param delta scroll wheel delta (positive = zoom in, negative = zoom out)
     */
    public void onScroll(double delta) {
        if (!isEnabled() || !zooming || !scroll.isEnabled()) return;
        scrollMultiplier = Math.max(0.1, Math.min(2.0, scrollMultiplier - delta * 0.1));
    }
}
