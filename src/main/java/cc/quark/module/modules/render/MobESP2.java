package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.Vec3d;

public class MobESP2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Max entity detection range (blocks)", 64.0, 8.0, 128.0));

    private final BoolSetting hostile = register(new BoolSetting(
            "Hostile", "Show hostile mobs (red)", true));

    private final BoolSetting passive = register(new BoolSetting(
            "Passive", "Show passive animals (green)", true));

    public MobESP2() {
        super("MobESP2", "Enhanced mob ESP with per-type colors", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null || mc.gameRenderer == null) return;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity.isRemoved()) continue;

            boolean isHostile = entity instanceof HostileEntity;
            boolean isPassive = entity instanceof AnimalEntity;

            if (isHostile && !hostile.isEnabled()) continue;
            if (isPassive && !passive.isEnabled()) continue;
            if (!isHostile && !isPassive) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;

            // Project entity position to screen
            Vec3d entityPos = entity.getLerpedPos(event.getTickDelta()).add(0, entity.getHeight() / 2.0, 0);
            int[] screen = worldToScreen(entityPos, screenW, screenH);
            if (screen == null) continue;

            int color = isHostile ? 0xFFFF4444 : 0xFF44FF88;
            String label = entity.getType().getName().getString()
                    + " " + String.format("%.0fm", dist);

            ctx.fill(screen[0] - 2, screen[1] - 2, screen[0] + 2, screen[1] + 2, color);
            ctx.drawTextWithShadow(mc.textRenderer, label, screen[0] + 4, screen[1] - 4, color);
        }
    }

    private int[] worldToScreen(Vec3d world, int sw, int sh) {
        // Simple projection via camera matrix
        net.minecraft.client.render.Camera cam = mc.gameRenderer.getCamera();
        Vec3d relative = world.subtract(cam.getPos());
        // Use a rough perspective projection
        double fov = Math.toRadians(mc.options.getFov().getValue());
        float yaw = (float) Math.toRadians(cam.getYaw());
        float pitch = (float) Math.toRadians(cam.getPitch());

        double cosYaw = Math.cos(yaw), sinYaw = Math.sin(yaw);
        double cosPitch = Math.cos(pitch), sinPitch = Math.sin(pitch);

        double rx = relative.x * cosYaw - relative.z * sinYaw;
        double ry = relative.y * cosPitch - (relative.x * sinYaw + relative.z * cosYaw) * sinPitch;
        double rz = relative.y * sinPitch + (relative.x * sinYaw + relative.z * cosYaw) * cosPitch;

        if (rz <= 0) return null; // behind camera

        double scale = (sw / 2.0) / Math.tan(fov / 2.0);
        int sx = (int) (sw / 2.0 + rx * scale / rz);
        int sy = (int) (sh / 2.0 - ry * scale / rz);

        if (sx < -100 || sx > sw + 100 || sy < -100 || sy > sh + 100) return null;
        return new int[]{sx, sy};
    }
}
