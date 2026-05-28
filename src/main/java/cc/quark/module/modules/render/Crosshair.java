package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

public class Crosshair extends Module {

    private final ModeSetting style = register(new ModeSetting("Style", "Crosshair style", "Plus", "Plus", "Dot", "Circle", "T", "Arrow"));
    private final IntSetting size = register(new IntSetting("Size", "Crosshair size", 5, 1, 20));
    private final ColorSetting color = register(new ColorSetting("Color", "Crosshair color", 0xFFFFFFFF));
    private final BoolSetting dynamicExpand = register(new BoolSetting("Dynamic", "Expand on movement", true));
    private final BoolSetting showDefault = register(new BoolSetting("Show Default", "Show vanilla crosshair too", false));

    public Crosshair() {
        super("Crosshair", "Custom crosshair renderer", Category.RENDER);
    }

    

    

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.options.getPerspective().isFirstPerson() == false) return;
        DrawContext ctx = event.getDrawContext();
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();
        int cx = sw / 2;
        int cy = sh / 2;
        int c = color.get();

        int s = size.get();
        if (dynamicExpand.isEnabled()) {
            Vec3d vel = mc.player.getVelocity();
            double spd = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            s += (int)(spd * 10);
        }

        switch (style.get()) {
            case "Plus" -> {
                ctx.fill(cx - s, cy - 1, cx + s, cy + 1, c);
                ctx.fill(cx - 1, cy - s, cx + 1, cy + s, c);
            }
            case "Dot" -> ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, c);
            case "Circle" -> drawCircle(ctx, cx, cy, s, c);
            case "T" -> {
                ctx.fill(cx - s, cy - 1, cx + s, cy + 1, c);
                ctx.fill(cx - 1, cy - 1, cx + 1, cy + s, c);
            }
            case "Arrow" -> {
                ctx.fill(cx, cy - s, cx + 1, cy, c);
                ctx.fill(cx - s/2, cy - s/2, cx + s/2 + 1, cy - s/2 + 1, c);
            }
        }
    }

    private void drawCircle(DrawContext ctx, int cx, int cy, int r, int color) {
        for (int angle = 0; angle < 360; angle += 10) {
            double rad = Math.toRadians(angle);
            int x = (int)(cx + r * Math.cos(rad));
            int y = (int)(cy + r * Math.sin(rad));
            ctx.fill(x, y, x + 1, y + 1, color);
        }
    }
}
