package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * HealthIndicator - Projects entity HP to the screen and renders it above each entity's head.
 */
public class HealthIndicator extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to show health (blocks)", 16.0, 2.0, 64.0));
    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "OnlyPlayers", "Only show for players", false));

    public HealthIndicator() {
        super("HealthIndicator", "Shows entity health above their head", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;

            float hp    = living.getHealth();
            float maxHp = living.getMaxHealth();

            Vec3d headPos = entity.getPos().add(0, entity.getHeight() + 0.3, 0);
            double[] screen = projectToScreen(headPos);
            if (screen == null) continue;

            // Health color: green → yellow → red
            float pct = hp / maxHp;
            int red   = (int)((1.0f - pct) * 255);
            int green = (int)(pct * 255);
            int textColor = 0xFF000000 | (red << 16) | (green << 8);

            String hpStr = String.format("%.1f", hp);
            int tx = (int)screen[0] - mc.textRenderer.getWidth(hpStr) / 2;
            int ty = (int)screen[1];
            ctx.drawTextWithShadow(mc.textRenderer, hpStr, tx, ty, textColor);
        }
    }

    private double[] projectToScreen(Vec3d worldPos) {
        try {
            var camera = mc.gameRenderer.getCamera();
            Vec3d cam  = camera.getPos();
            double dx  = worldPos.x - cam.x;
            double dy  = worldPos.y - cam.y;
            double dz  = worldPos.z - cam.z;
            float yaw   = (float)Math.toRadians(camera.getYaw());
            float pitch = (float)Math.toRadians(camera.getPitch());
            double cosY = Math.cos(yaw), sinY = Math.sin(yaw);
            double cosP = Math.cos(pitch), sinP = Math.sin(pitch);
            double rx =  dx * cosY - dz * sinY;
            double ry = -dx * sinY * sinP + dy * cosP + dz * cosY * sinP;
            double rz =  dx * sinY * cosP + dy * sinP - dz * cosY * cosP;
            if (rz <= 0) return null;
            int sw = mc.getWindow().getScaledWidth();
            int sh = mc.getWindow().getScaledHeight();
            double fov = Math.toRadians(mc.options.getFov().getValue());
            double px  = (rx / (rz * Math.tan(fov / 2))) * (sw / 2.0) + sw / 2.0;
            double py  = (-ry / (rz * Math.tan(fov / 2))) * (sh / 2.0) + sh / 2.0;
            return new double[]{px, py};
        } catch (Exception e) { return null; }
    }
}
