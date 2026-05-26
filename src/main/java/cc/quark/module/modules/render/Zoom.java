package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public class Zoom extends Module {

    private final DoubleSetting fov = register(new DoubleSetting(
            "FOV", "Target FOV when zoomed in (degrees)", 10.0, 1.0, 50.0));

    private final BoolSetting smooth = register(new BoolSetting(
            "Smooth", "Smoothly interpolate FOV change", true));

    private final BoolSetting scroll = register(new BoolSetting(
            "Scroll Zoom", "Allow scroll wheel to adjust zoom level", true));

    private final BoolSetting cinemaMode = register(new BoolSetting(
            "Cinema Mode", "Reduce mouse sensitivity while zoomed for cinematic pan", false));

    private final BoolSetting nightVision = register(new BoolSetting(
            "Night Vision", "Apply night vision effect while zoomed", false));

    private static final int ZOOM_KEY = GLFW.GLFW_KEY_C;

    private double savedFov    = 70.0;
    private double currentFov  = 70.0;
    private double targetFov   = 70.0;
    private double scrolledFov = -1.0;
    private boolean zooming    = false;

    public Zoom() {
        super("Zoom", "Hold C to zoom in — scroll to adjust zoom level", Category.RENDER);
    }

    @Override
    public void onEnable() {
        zooming     = false;
        scrolledFov = -1.0;
        if (mc.options != null) {
            savedFov   = mc.options.getFov().getValue();
            currentFov = savedFov;
            targetFov  = savedFov;
        }
    }

    @Override
    public void onDisable() {
        zooming     = false;
        scrolledFov = -1.0;
        if (mc.options != null) {
            mc.options.getFov().setValue((int) Math.round(savedFov));
        }
        currentFov = savedFov;
        targetFov  = savedFov;
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.options == null) return;

        boolean keyHeld = GLFW.glfwGetKey(mc.getWindow().getHandle(), ZOOM_KEY) == GLFW.GLFW_PRESS;

        if (keyHeld && !zooming) {
            savedFov    = mc.options.getFov().getValue();
            zooming     = true;
            scrolledFov = -1.0;
        } else if (!keyHeld && zooming) {
            zooming     = false;
            scrolledFov = -1.0;
        }

        if (zooming) {
            targetFov = scrolledFov > 0 ? scrolledFov : fov.get();
        } else {
            targetFov = savedFov;
        }

        if (smooth.isEnabled()) {
            currentFov += (targetFov - currentFov) * 0.15;
            if (Math.abs(currentFov - targetFov) < 0.05) currentFov = targetFov;
        } else {
            currentFov = targetFov;
        }

        currentFov = Math.max(1.0, Math.min(110.0, currentFov));

        if (nightVision.isEnabled() && zooming) {
            StatusEffectInstance cur = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (cur == null || cur.getDuration() < 100) {
                mc.player.addStatusEffect(
                        new StatusEffectInstance(StatusEffects.NIGHT_VISION, 9999, 0, false, false, false));
            }
        } else if (!zooming) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    public double getModifiedFov(double original) {
        if (!isEnabled()) return original;
        if (!zooming && Math.abs(currentFov - savedFov) < 0.5) return original;
        return currentFov;
    }

    public boolean isCinemaMode() {
        return isEnabled() && zooming && cinemaMode.isEnabled();
    }

    public void onScroll(double delta) {
        if (!isEnabled() || !zooming || !scroll.isEnabled()) return;
        double base = scrolledFov > 0 ? scrolledFov : fov.get();
        scrolledFov = Math.max(1.0, Math.min(70.0, base - delta * 2.0));
    }
}
