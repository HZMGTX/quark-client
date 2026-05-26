package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ColorUtil;
import cc.quark.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Tracers extends Module {

    private final BoolSetting targetPlayers = register(new BoolSetting(
            "Players", "Draw tracers to other players", true));

    private final BoolSetting targetMobs = register(new BoolSetting(
            "Mobs", "Draw tracers to hostile mobs", true));

    private final BoolSetting targetAnimals = register(new BoolSetting(
            "Animals", "Draw tracers to passive animals", false));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum tracer range in blocks", 64, 8, 100));

    private final DoubleSetting width = register(new DoubleSetting(
            "Width", "Tracer line width in pixels", 1.0, 1.0, 3.0));

    private final BoolSetting healthColor = register(new BoolSetting(
            "HealthColor", "Color tracers by health (green-yellow-red)", true));

    public Tracers() {
        super("Tracers", "Draws lines from camera to entities", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        Vec3d origin = mc.player.getEyePos();
        float td = event.getTickDelta();
        float lineWidth = (float) width.get();
        double maxRange = range.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity.isInvisible()) continue;
            if (!(entity instanceof LivingEntity)) continue;

            float r, g, b;

            if (entity instanceof PlayerEntity player) {
                if (!targetPlayers.isEnabled()) continue;
                if (healthColor.isEnabled()) {
                    float pct = player.getMaxHealth() > 0 ? player.getHealth() / player.getMaxHealth() : 1f;
                    int hc = ColorUtil.healthColor(pct);
                    r = ((hc >> 16) & 0xFF) / 255f;
                    g = ((hc >> 8) & 0xFF) / 255f;
                    b = (hc & 0xFF) / 255f;
                } else {
                    r = 1.0f; g = 0.2f; b = 0.2f;
                }
            } else if (entity instanceof AnimalEntity animal) {
                if (!targetAnimals.isEnabled()) continue;
                if (healthColor.isEnabled()) {
                    float pct = animal.getMaxHealth() > 0 ? animal.getHealth() / animal.getMaxHealth() : 1f;
                    int hc = ColorUtil.healthColor(pct);
                    r = ((hc >> 16) & 0xFF) / 255f;
                    g = ((hc >> 8) & 0xFF) / 255f;
                    b = (hc & 0xFF) / 255f;
                } else {
                    r = 1.0f; g = 1.0f; b = 0.2f;
                }
            } else if (entity instanceof MobEntity mob) {
                if (!targetMobs.isEnabled()) continue;
                if (healthColor.isEnabled()) {
                    float pct = mob.getMaxHealth() > 0 ? mob.getHealth() / mob.getMaxHealth() : 1f;
                    int hc = ColorUtil.healthColor(pct);
                    r = ((hc >> 16) & 0xFF) / 255f;
                    g = ((hc >> 8) & 0xFF) / 255f;
                    b = (hc & 0xFF) / 255f;
                } else {
                    r = 1.0f; g = 0.6f; b = 0.0f;
                }
            } else {
                continue;
            }

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;

            if (origin.distanceTo(new Vec3d(ex, ey, ez)) > maxRange) continue;

            Vec3d target = new Vec3d(ex, ey + entity.getHeight() * 0.5, ez);
            RenderUtil.drawLine3D(event.getMatrixStack(), origin, target, r, g, b, 0.7f, lineWidth);
        }
    }
}
