package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

/**
 * Ambience - tints the entire screen with a configurable colour overlay.
 *
 * Modes:
 *   Static  - constant tint from the ColorSetting
 *   Pulse   - alpha oscillates like a slow heartbeat
 *   Rainbow - hue cycles through the spectrum over time
 */
public class Ambience extends Module {

    private final ColorSetting tintColor = register(new ColorSetting(
            "Tint Color", "Overlay color (ARGB)", 0x4000AAFF));

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Tint animation mode", "Static", "Static", "Pulse", "Rainbow"));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Animation speed for Pulse/Rainbow", 1.0, 0.1, 5.0));

    public Ambience() {
        super("Ambience", "Screen colour tint overlay with Static, Pulse, and Rainbow modes", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.getWindow() == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int argb;

        switch (mode.get()) {
            case "Pulse" -> {
                long period = (long)(3000 / speed.get());
                double t = (System.currentTimeMillis() % period) / (double) period;
                float pulseAlpha = (float)(0.5 + 0.5 * Math.sin(t * Math.PI * 2));
                int baseA = tintColor.getAlpha();
                int a = (int)(baseA * pulseAlpha);
                argb = ColorSetting.pack(a, tintColor.getRed(), tintColor.getGreen(), tintColor.getBlue());
            }
            case "Rainbow" -> {
                long period = (long)(5000 / speed.get());
                float hue = (System.currentTimeMillis() % period) / (float) period;
                int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
                int a = tintColor.getAlpha();
                argb = (a << 24) | (rgb & 0x00FFFFFF);
            }
            default -> argb = tintColor.get(); // Static
        }

        ctx.fill(0, 0, sw, sh, argb);
    }
}
