package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class EntityFade extends Module {

    private final DoubleSetting startDist = register(new DoubleSetting(
            "Start Distance", "Distance at which entities begin to fade (blocks)", 32.0, 4.0, 128.0));

    private final DoubleSetting endDist = register(new DoubleSetting(
            "End Distance", "Distance at which entities are fully faded (invisible)", 64.0, 8.0, 256.0));

    public EntityFade() {
        super("EntityFade", "Fades out entities that are far away", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null || mc.gameRenderer == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        double sd = startDist.get();
        double ed = endDist.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || entity.isRemoved()) continue;
            if (!(entity instanceof LivingEntity)) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist <= sd) continue;

            // Calculate fade alpha (1.0 at sd, 0.0 at ed)
            float fade = (float) Math.max(0, 1.0 - (dist - sd) / (ed - sd));
            if (fade <= 0) continue;

            // Draw a dark overlay dot to simulate fading
            Vec3d pos = entity.getLerpedPos(event.getTickDelta()).add(0, entity.getHeight() / 2.0, 0);
            int[] screen = worldToScreen(pos, sw, sh);
            if (screen == null) continue;

            // Overlay with inverse alpha to fade
            int alpha = (int) ((1f - fade) * 180);
            int overlay = (alpha << 24) | 0x000000;
            ctx.fill(screen[0] - 4, screen[1] - 8, screen[0] + 4, screen[1] + 8, overlay);
        }
    }

    private int[] worldToScreen(Vec3d world, int sw, int sh) {
        net.minecraft.client.render.Camera cam = mc.gameRenderer.getCamera();
        Vec3d rel = world.subtract(cam.getPos());
        double fov = Math.toRadians(mc.options.getFov().getValue());
        float yaw = (float) Math.toRadians(cam.getYaw());
        float pitch = (float) Math.toRadians(cam.getPitch());

        double cosYaw = Math.cos(yaw), sinYaw = Math.sin(yaw);
        double cosPitch = Math.cos(pitch), sinPitch = Math.sin(pitch);

        double rx = rel.x * cosYaw - rel.z * sinYaw;
        double ry = rel.y * cosPitch - (rel.x * sinYaw + rel.z * cosYaw) * sinPitch;
        double rz = rel.y * sinPitch + (rel.x * sinYaw + rel.z * cosYaw) * cosPitch;

        if (rz <= 0) return null;
        double scale = (sw / 2.0) / Math.tan(fov / 2.0);
        int sx = (int) (sw / 2.0 + rx * scale / rz);
        int sy = (int) (sh / 2.0 - ry * scale / rz);
        if (sx < 0 || sx > sw || sy < 0 || sy > sh) return null;
        return new int[]{sx, sy};
    }
}
