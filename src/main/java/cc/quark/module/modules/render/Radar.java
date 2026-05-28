package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.gui.ClickGUI;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

public class Radar extends Module {

    private final IntSetting posX = register(new IntSetting("X", "Radar X position", 10, 0, 3000));
    private final IntSetting posY = register(new IntSetting("Y", "Radar Y position", 10, 0, 3000));
    private final IntSetting size = register(new IntSetting("Size", "Radar radius in pixels", 50, 20, 150));
    private final DoubleSetting range = register(new DoubleSetting("Range", "World range shown on radar in blocks", 64.0, 16.0, 256.0));
    private final BoolSetting players = register(new BoolSetting("Players", "Show other players on radar", true));
    private final BoolSetting mobs = register(new BoolSetting("Mobs", "Show mobs on radar", false));
    private final ModeSetting style = register(new ModeSetting("Style", "Radar shape", "Circle", "Circle", "Square"));

    public Radar() {
        super("Radar", "Mini radar showing nearby entities", Category.RENDER);
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (mc.player == null || mc.world == null) return;

        DrawContext ctx = event.getDrawContext();
        int cx = posX.get() + size.get();
        int cy = posY.get() + size.get();
        int r = size.get();
        int accentColor = ClickGUI.getAccentColor();
        boolean isCircle = "Circle".equals(style.get());

        // Background
        if (isCircle) {
            drawFilledCircle(ctx, cx, cy, r, 0xAA111111);
        } else {
            ctx.fill(cx - r, cy - r, cx + r, cy + r, 0xAA111111);
        }

        // Border ring in accent color
        if (isCircle) {
            drawCircleOutline(ctx, cx, cy, r, accentColor);
        } else {
            RenderUtil.draw2DBox(ctx, cx - r, cy - r, r * 2, r * 2, accentColor, 0);
        }

        // N/S/E/W labels
        float yaw = mc.player.getYaw();
        RenderUtil.drawCustomText(ctx, "N", cx - 2, cy - r + 2, 0xFFAAAAAA);
        RenderUtil.drawCustomText(ctx, "S", cx - 2, cy + r - 9, 0xFFAAAAAA);
        RenderUtil.drawCustomText(ctx, "W", cx - r + 2, cy - 4, 0xFFAAAAAA);
        RenderUtil.drawCustomText(ctx, "E", cx + r - 6, cy - 4, 0xFFAAAAAA);

        // Entities
        double worldRange = range.get();
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();
        float yawRad = (float) Math.toRadians(yaw);

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (entity.isRemoved()) continue;

            boolean isPlayer = entity instanceof PlayerEntity;
            boolean isHostile = entity instanceof HostileEntity;
            boolean isPassive = entity instanceof AnimalEntity;

            if (isPlayer && !players.isEnabled()) continue;
            if ((isHostile || isPassive) && !mobs.isEnabled()) continue;
            if (!isPlayer && !isHostile && !isPassive) continue;

            double dx = entity.getX() - playerX;
            double dz = entity.getZ() - playerZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > worldRange) continue;

            // Rotate by player yaw so forward is up
            double rotDx =  dx * Math.cos(-yawRad) - dz * Math.sin(-yawRad);
            double rotDz =  dx * Math.sin(-yawRad) + dz * Math.cos(-yawRad);

            int dotX = cx + (int)(rotDx / worldRange * r);
            int dotY = cy + (int)(rotDz / worldRange * r);

            // Clip to radar bounds
            if (isCircle) {
                double ddx = dotX - cx;
                double ddy = dotY - cy;
                if (ddx * ddx + ddy * ddy > (double)(r - 3) * (r - 3)) continue;
            } else {
                if (dotX < cx - r + 2 || dotX > cx + r - 2) continue;
                if (dotY < cy - r + 2 || dotY > cy + r - 2) continue;
            }

            int dotColor;
            if (isPlayer) {
                dotColor = 0xFFFFFF44;
            } else if (isHostile) {
                dotColor = 0xFFFF4444;
            } else {
                dotColor = 0xFF44FF88;
            }

            ctx.fill(dotX - 1, dotY - 1, dotX + 2, dotY + 2, dotColor);
        }

        // Center dot for the player (white)
        ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFFFFFF);
    }

    private void drawFilledCircle(DrawContext ctx, int cx, int cy, int r, int color) {
        for (int px = -r; px <= r; px++) {
            for (int py = -r; py <= r; py++) {
                if (px * px + py * py <= r * r) {
                    ctx.fill(cx + px, cy + py, cx + px + 1, cy + py + 1, color);
                }
            }
        }
    }

    private void drawCircleOutline(DrawContext ctx, int cx, int cy, int r, int color) {
        int rSqOuter = r * r;
        int rSqInner = (r - 1) * (r - 1);
        for (int px = -r; px <= r; px++) {
            for (int py = -r; py <= r; py++) {
                int sq = px * px + py * py;
                if (sq <= rSqOuter && sq >= rSqInner) {
                    ctx.fill(cx + px, cy + py, cx + px + 1, cy + py + 1, color);
                }
            }
        }
    }
}
