package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * ShaderESP - Entity ESP that draws a glow-style box/label around entities.
 * True shader glow requires a post-processing pipeline; this implementation
 * draws layered semi-transparent boxes to simulate a glow effect.
 */
public class ShaderESP extends Module {

    private final ColorSetting color = register(new ColorSetting(
            "Color", "ESP glow color", 0xFF00FF00));
    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "OnlyPlayers", "Only highlight players", true));

    public ShaderESP() {
        super("ShaderESP", "Entity ESP with shader glow effect", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        DrawContext ctx = event.getDrawContext();

        int c = color.get();
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (onlyPlayers.isEnabled() && !(entity instanceof PlayerEntity)) continue;

            Vec3d pos = entity.getPos();
            // Project entity to screen
            double[] screenPos = projectToScreen(pos.add(0, entity.getHeight() / 2.0, 0));
            double[] screenFeet = projectToScreen(pos);
            double[] screenHead = projectToScreen(pos.add(0, entity.getHeight(), 0));
            if (screenPos == null || screenFeet == null || screenHead == null) continue;

            int sx = (int) screenPos[0];
            int sy = (int) screenPos[1];
            int height = (int) Math.max(10, Math.abs(screenFeet[1] - screenHead[1]));
            int width  = (int) (height * 0.5);

            // Draw layered boxes for glow effect
            for (int layer = 3; layer >= 0; layer--) {
                int expand = layer * 2;
                int alpha = (int) (a * 255 * (1.0f - layer * 0.2f)) & 0xFF;
                int layerColor = (alpha << 24) | (c & 0x00FFFFFF);
                int x1 = sx - width / 2 - expand;
                int y1 = sy - height / 2 - expand;
                int x2 = sx + width / 2 + expand;
                int y2 = sy + height / 2 + expand;
                ctx.fill(x1, y1, x2, y1 + 1, layerColor);
                ctx.fill(x1, y2 - 1, x2, y2, layerColor);
                ctx.fill(x1, y1, x1 + 1, y2, layerColor);
                ctx.fill(x2 - 1, y1, x2, y2, layerColor);
            }

            // Name label
            if (entity instanceof PlayerEntity player) {
                String name = player.getGameProfile().getName();
                ctx.drawTextWithShadow(mc.textRenderer, name,
                        sx - mc.textRenderer.getWidth(name) / 2, sy - height / 2 - 10, c);
            }
        }
    }

    private double[] projectToScreen(Vec3d worldPos) {
        try {
            net.minecraft.client.render.Camera camera = mc.gameRenderer.getCamera();
            Vec3d camPos = camera.getPos();

            // Simple MVP projection stub (requires GLU/actual matrix math for accuracy)
            // This provides an approximate screen position
            double dx = worldPos.x - camPos.x;
            double dy = worldPos.y - camPos.y;
            double dz = worldPos.z - camPos.z;

            float yaw   = (float) Math.toRadians(camera.getYaw());
            float pitch = (float) Math.toRadians(camera.getPitch());

            double cosY = Math.cos(yaw), sinY = Math.sin(yaw);
            double cosP = Math.cos(pitch), sinP = Math.sin(pitch);

            double rx =  dx * cosY - dz * sinY;
            double ry = -dx * sinY * sinP + dy * cosP + dz * cosY * sinP;
            double rz =  dx * sinY * cosP + dy * sinP - dz * cosY * cosP;

            if (rz <= 0) return null; // Behind camera

            int screenW = mc.getWindow().getScaledWidth();
            int screenH = mc.getWindow().getScaledHeight();
            double fov  = Math.toRadians(mc.options.getFov().getValue());

            double px = (rx / (rz * Math.tan(fov / 2))) * (screenW / 2.0) + screenW / 2.0;
            double py = (-ry / (rz * Math.tan(fov / 2))) * (screenH / 2.0) + screenH / 2.0;
            return new double[]{px, py};
        } catch (Exception e) {
            return null;
        }
    }
}
