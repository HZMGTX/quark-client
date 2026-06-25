package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

/**
 * OverlayESP — renders a full-screen 2D target tracking overlay for nearby
 * players, showing directional indicators (arrows) and distance rings that
 * point toward each target from the player's perspective.
 */
public class OverlayESP extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum tracking range in blocks", 64.0, 10.0, 256.0));

    private final IntSetting maxTargets = register(new IntSetting(
            "Max Targets", "Maximum number of tracked targets to display", 5, 1, 20));

    private final ColorSetting targetColor = register(new ColorSetting(
            "Color", "Target indicator color", 0xFFFF4444));

    private final BoolSetting showDistance = register(new BoolSetting(
            "Distance", "Show distance next to indicator", true));

    private final BoolSetting showName = register(new BoolSetting(
            "Name", "Show player name on indicator", true));

    private final ModeSetting indicatorMode = register(new ModeSetting(
            "Indicator", "Arrow style on screen edge", "Arrow", "Arrow", "Dot", "Box"));

    private final DoubleSetting radarRadius = register(new DoubleSetting(
            "Radar Radius", "Radius of the direction ring in pixels", 80.0, 40.0, 200.0));

    private final BoolSetting showRadarRing = register(new BoolSetting(
            "Radar Ring", "Draw a circular ring to place indicators on", true));

    public OverlayESP() {
        super("OverlayESP", "Full-screen overlay for target tracking", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;
        ClientPlayerEntity self = mc.player;

        DrawContext ctx = event.getDrawContext();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();
        int cx = screenW / 2;
        int cy = screenH / 2;

        // Collect nearby players sorted by distance
        List<PlayerEntity> targets = mc.world.getPlayers().stream()
                .filter(p -> p != self)
                .filter(p -> self.distanceTo(p) <= range.get())
                .sorted(Comparator.comparingDouble(self::distanceTo))
                .limit(maxTargets.get())
                .toList();

        if (targets.isEmpty()) return;

        // Draw radar ring
        if (showRadarRing.isEnabled()) {
            drawCircle(ctx, cx, cy, (int) radarRadius.get(), 0x44FFFFFF);
        }

        float playerYaw = self.getYaw(); // degrees, 0=south, 90=west, -90=east, 180=north

        for (PlayerEntity target : targets) {
            double dx = target.getX() - self.getX();
            double dz = target.getZ() - self.getZ();
            double dist = self.distanceTo(target);

            // Angle from player to target relative to player yaw (in screen coords)
            // World: +Z = south, +X = east; yaw 0 = south, increases counterclockwise
            double worldAngle = Math.atan2(dz, dx); // radians, east=0, north=-PI/2
            // Convert to screen angle: playerYaw=0 → south (down on minimap)
            double yawRad = Math.toRadians(playerYaw + 90); // +90 to align with screen
            double relAngle = worldAngle - yawRad; // relative to camera direction

            // Position on the radar ring
            int indX = cx + (int)(Math.cos(relAngle) * radarRadius.get());
            int indY = cy + (int)(Math.sin(relAngle) * radarRadius.get());

            int color = targetColor.get() | 0xFF000000;

            // Draw indicator
            switch (indicatorMode.get()) {
                case "Arrow" -> drawArrow(ctx, indX, indY, relAngle, color);
                case "Dot"   -> ctx.fill(indX - 3, indY - 3, indX + 3, indY + 3, color);
                case "Box"   -> {
                    ctx.fill(indX - 5, indY - 5, indX + 5, indY + 5, color & 0x88FFFFFF);
                    ctx.fill(indX - 5, indY - 5, indX + 5, indY - 4, color);
                    ctx.fill(indX - 5, indY + 4, indX + 5, indY + 5, color);
                    ctx.fill(indX - 5, indY - 5, indX - 4, indY + 5, color);
                    ctx.fill(indX + 4, indY - 5, indX + 5, indY + 5, color);
                }
            }

            // Labels near indicator
            int textY = indY + 8;
            if (showName.isEnabled()) {
                String name = target.getGameProfile().getName();
                int tw = mc.textRenderer.getWidth(name);
                RenderUtil.drawCustomText(ctx, name, indX - tw / 2, textY, color);
                textY += mc.textRenderer.fontHeight + 1;
            }
            if (showDistance.isEnabled()) {
                String distStr = String.format("%.0fm", dist);
                int tw = mc.textRenderer.getWidth(distStr);
                RenderUtil.drawCustomText(ctx, distStr, indX - tw / 2, textY, 0xFFAAAAAA);
            }
        }
    }

    /** Draws a 4-vertex filled triangle (arrow) pointing in the given direction. */
    private void drawArrow(DrawContext ctx, int x, int y, double angle, int color) {
        // Tip of the arrow
        int tx = (int)(x + Math.cos(angle) * 7);
        int ty = (int)(y + Math.sin(angle) * 7);
        // Two back corners
        int bx1 = (int)(x + Math.cos(angle + 2.2) * 5);
        int by1 = (int)(y + Math.sin(angle + 2.2) * 5);
        int bx2 = (int)(x + Math.cos(angle - 2.2) * 5);
        int by2 = (int)(y + Math.sin(angle - 2.2) * 5);

        // Simple triangle fill using ctx.fill for each row — use a border approach
        ctx.fill(x - 4, y - 4, x + 4, y + 4, color & 0x88FFFFFF);
        ctx.fill(tx - 2, ty - 2, tx + 2, ty + 2, color);
        ctx.fill(bx1 - 2, by1 - 2, bx1 + 2, by1 + 2, color);
        ctx.fill(bx2 - 2, by2 - 2, bx2 + 2, by2 + 2, color);
    }

    /** Draws a rough circle outline using small rectangles. */
    private void drawCircle(DrawContext ctx, int cx, int cy, int radius, int color) {
        int steps = 64;
        for (int i = 0; i < steps; i++) {
            double angle = (Math.PI * 2 * i) / steps;
            int px = cx + (int)(Math.cos(angle) * radius);
            int py = cy + (int)(Math.sin(angle) * radius);
            ctx.fill(px - 1, py - 1, px + 1, py + 1, color);
        }
    }
}
