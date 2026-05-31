package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;

public class CustomCrosshair extends Module {

    private final ModeSetting  style = register(new ModeSetting("Style", "Crosshair shape", "Cross", "Cross", "Dot", "Circle", "Square"));
    private final ColorSetting color = register(new ColorSetting("Color", "Crosshair color", 0xFFFFFFFF));

    public CustomCrosshair() {
        super("CustomCrosshair", "Draws a custom crosshair in the selected style", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;
        int col = color.get();

        switch (style.get()) {
            case "Cross" -> {
                ctx.fill(cx - 6, cy - 1, cx + 6, cy + 1, col);
                ctx.fill(cx - 1, cy - 6, cx + 1, cy + 6, col);
            }
            case "Dot" -> ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, col);
            case "Circle" -> {
                int r = 5;
                for (int deg = 0; deg < 360; deg += 5) {
                    double rad = Math.toRadians(deg);
                    int px = (int) (cx + r * Math.cos(rad));
                    int py = (int) (cy + r * Math.sin(rad));
                    ctx.fill(px, py, px + 1, py + 1, col);
                }
            }
            case "Square" -> {
                int hs = 5;
                ctx.fill(cx - hs, cy - hs, cx + hs, cy - hs + 1, col);
                ctx.fill(cx - hs, cy + hs - 1, cx + hs, cy + hs, col);
                ctx.fill(cx - hs, cy - hs, cx - hs + 1, cy + hs, col);
                ctx.fill(cx + hs - 1, cy - hs, cx + hs, cy + hs, col);
            }
        }
    }
}
