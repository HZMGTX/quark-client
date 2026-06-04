package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.math.Vec3d;

/**
 * ProjectileTracer - Draws lines from the screen center toward tracked projectiles
 * in 2D, and labels them with their distance.
 */
public class ProjectileTracer extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Tracer line color", 0xFFFFFF00));
    private final DoubleSetting length = register(new DoubleSetting(
            "Length", "Maximum tracer length (blocks)", 20.0, 4.0, 64.0));

    public ProjectileTracer() {
        super("ProjectileTracer", "Shows trajectory lines for projectiles", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        int c = color.get();

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ProjectileEntity)) continue;
            if (!(entity instanceof ArrowEntity) && !(entity instanceof TridentEntity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > length.get()) continue;

            double[] screen = projectToScreen(entity.getPos());
            if (screen == null) continue;

            int ex = (int) screen[0];
            int ey = (int) screen[1];

            // Draw tracer line
            drawLine2D(ctx, cx, cy, ex, ey, c);

            // Distance label
            String label = String.format("%.0fm", dist);
            ctx.drawTextWithShadow(mc.textRenderer, label,
                    ex - mc.textRenderer.getWidth(label) / 2, ey - 8, c);
        }
    }

    private void drawLine2D(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        int dx = x2 - x1, dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) return;
        float sx = (float) dx / steps;
        float sy = (float) dy / steps;
        for (int i = 0; i <= steps; i += 2) {
            int px = x1 + (int)(sx * i);
            int py = y1 + (int)(sy * i);
            ctx.fill(px, py, px + 1, py + 1, color);
        }
    }

    private double[] projectToScreen(Vec3d worldPos) {
        try {
            var camera = mc.gameRenderer.getCamera();
            Vec3d cam = camera.getPos();
            double dx = worldPos.x - cam.x, dy = worldPos.y - cam.y, dz = worldPos.z - cam.z;
            float yaw = (float)Math.toRadians(camera.getYaw());
            float pitch = (float)Math.toRadians(camera.getPitch());
            double cosY = Math.cos(yaw), sinY = Math.sin(yaw);
            double cosP = Math.cos(pitch), sinP = Math.sin(pitch);
            double rx =  dx * cosY - dz * sinY;
            double ry = -dx * sinY * sinP + dy * cosP + dz * cosY * sinP;
            double rz =  dx * sinY * cosP + dy * sinP - dz * cosY * cosP;
            if (rz <= 0) return null;
            int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
            double fov = Math.toRadians(mc.options.getFov().getValue());
            return new double[]{
                    (rx / (rz * Math.tan(fov/2))) * (sw/2.0) + sw/2.0,
                    (-ry / (rz * Math.tan(fov/2))) * (sh/2.0) + sh/2.0
            };
        } catch (Exception e) { return null; }
    }
}
