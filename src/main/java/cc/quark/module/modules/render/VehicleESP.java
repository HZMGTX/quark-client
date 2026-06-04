package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.util.math.Vec3d;

public class VehicleESP extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Detection range for vehicles (blocks)", 64.0, 16.0, 128.0));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP color for vehicles", 0xFF55AAFF));

    public VehicleESP() {
        super("VehicleESP", "Shows boats, minecarts, horses", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null || mc.gameRenderer == null) return;

        DrawContext ctx = event.getDrawContext();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int argb = color.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity.isRemoved()) continue;
            boolean isVehicle = entity instanceof BoatEntity
                    || entity instanceof AbstractMinecartEntity
                    || entity instanceof HorseBaseEntity;
            if (!isVehicle) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;

            Vec3d pos = entity.getLerpedPos(event.getTickDelta()).add(0, entity.getHeight() / 2.0, 0);
            int[] screen = worldToScreen(pos, sw, sh);
            if (screen == null) continue;

            String label = entity.getType().getName().getString()
                    + " " + String.format("%.0fm", dist);

            ctx.fill(screen[0] - 3, screen[1] - 3, screen[0] + 3, screen[1] + 3, argb);
            ctx.drawTextWithShadow(mc.textRenderer, label, screen[0] + 5, screen[1] - 4, argb);
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
        if (sx < -200 || sx > sw + 200 || sy < -200 || sy > sh + 200) return null;
        return new int[]{sx, sy};
    }
}
