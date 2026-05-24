package com.ghostclient.module.modules.render;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventRender3D;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.DoubleSetting;
import com.ghostclient.util.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Tracers - draws lines from the player's camera to nearby entities.
 */
public class Tracers extends Module {

    private final BoolSetting targetPlayers = register(new BoolSetting(
            "Players", "Draw tracers to other players", true));

    private final BoolSetting targetMobs = register(new BoolSetting(
            "Mobs", "Draw tracers to hostile mobs", true));

    private final BoolSetting targetAnimals = register(new BoolSetting(
            "Animals", "Draw tracers to passive animals", false));

    private final DoubleSetting width = register(new DoubleSetting(
            "Width", "Tracer line width in pixels", 1.0, 1.0, 3.0));

    public Tracers() {
        super("Tracers", "Draws lines from camera to entities", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        Vec3d origin = mc.player.getEyePos();
        float td = event.getTickDelta();
        float lineWidth = (float) width.get();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity.isInvisible()) continue;

            float r, g, b;

            if (entity instanceof PlayerEntity) {
                if (!targetPlayers.isEnabled()) continue;
                r = 1.0f; g = 0.2f; b = 0.2f;
            } else if (entity instanceof AnimalEntity) {
                if (!targetAnimals.isEnabled()) continue;
                r = 1.0f; g = 1.0f; b = 0.2f;
            } else if (entity instanceof MobEntity) {
                if (!targetMobs.isEnabled()) continue;
                r = 1.0f; g = 0.6f; b = 0.0f;
            } else {
                continue;
            }

            double ex = entity.prevX + (entity.getX() - entity.prevX) * td;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * td;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * td;
            Vec3d target = new Vec3d(ex, ey + entity.getHeight() * 0.5, ez);

            RenderUtil.drawLine3D(event.getMatrixStack(), origin, target, r, g, b, 0.7f, lineWidth);
        }
    }
}
