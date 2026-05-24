package com.ghostclient.module.modules.render;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventRender3D;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.ColorSetting;
import com.ghostclient.setting.DoubleSetting;
import com.ghostclient.setting.ModeSetting;
import com.ghostclient.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * ESP – draws axis-aligned bounding boxes (and optional fill) around entities.
 *
 * Modes:
 *   Box    – full wireframe box
 *   Corner – only the 8 corner L-shapes
 *   2D     – screen-space 2-D rectangle projected from the 3-D bounding box
 *   Shader – same as Box with glow-style thicker lines
 */
public class ESP extends Module {

    // ── Entity type toggles ───────────────────────────────────────────────────
    private final BoolSetting players  = register(new BoolSetting("Players",  "ESP for other players",          true));
    private final BoolSetting mobs     = register(new BoolSetting("Mobs",     "ESP for hostile mobs",           true));
    private final BoolSetting animals  = register(new BoolSetting("Animals",  "ESP for passive animals",        false));
    private final BoolSetting items    = register(new BoolSetting("Items",    "ESP for dropped items",          false));
    private final BoolSetting vehicles = register(new BoolSetting("Vehicles", "ESP for boats and minecarts",    false));

    // ── Visual settings ───────────────────────────────────────────────────────
    private final ModeSetting mode        = register(new ModeSetting("Mode",   "ESP draw mode",                 "Box",  "Box","Corner","2D","Shader"));
    private final BoolSetting fill        = register(new BoolSetting("Fill",   "Fill the ESP box",              true));
    private final BoolSetting outline     = register(new BoolSetting("Outline","Draw the ESP box outline",      true));
    private final BoolSetting tracers     = register(new BoolSetting("Tracers","Draw tracer lines to entities", false));

    // ── Colors ────────────────────────────────────────────────────────────────
    private final ColorSetting playerColor  = register(new ColorSetting("PlayerColor",  "Player ESP colour",  0xFFFF0000));
    private final ColorSetting mobColor     = register(new ColorSetting("MobColor",     "Mob ESP colour",     0xFFFF8800));
    private final ColorSetting friendColor  = register(new ColorSetting("FriendColor",  "Friend ESP colour",  0xFF00FF00));
    private final ColorSetting animalColor  = register(new ColorSetting("AnimalColor",  "Animal ESP colour",  0xFFFFFF00));

    private final DoubleSetting fillAlpha  = register(new DoubleSetting("FillAlpha",  "Fill transparency (0-255)", 30, 0, 255));

    public ESP() {
        super("ESP", "Draws boxes around entities through walls", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices   = event.getMatrixStack();
        float        tickDelta = event.getTickDelta();

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player)        continue;
            if (entity.isInvisible())       continue;

            ColorSetting chosenColor = resolveColor(entity);
            if (chosenColor == null)        continue;   // not a wanted entity type

            float r = chosenColor.getRedF();
            float g = chosenColor.getGreenF();
            float b = chosenColor.getBlueF();
            float fa = (float)(fillAlpha.get() / 255.0);

            // Interpolated bounding box
            double ex = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
            double ey = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
            double ez = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
            Box box = entity.getBoundingBox().offset(
                    ex - entity.getX(), ey - entity.getY(), ez - entity.getZ());

            if (mode.is("Box") || mode.is("Shader")) {
                float lineW = mode.is("Shader") ? 2.5f : 1.5f;
                if (fill.isEnabled())    RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
                if (outline.isEnabled()) RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, lineW);

            } else if (mode.is("Corner")) {
                if (fill.isEnabled())    RenderUtil.drawFilledBox(matrices, box, r, g, b, fa);
                if (outline.isEnabled()) drawCornerBox(matrices, box, r, g, b, 0.9f, 1.5f);

            } else if (mode.is("2D")) {
                draw2DESP(matrices, box, r, g, b, tickDelta);
            }

            if (tracers.isEnabled()) {
                Vec3d center = new Vec3d(ex, ey + entity.getHeight() * 0.5, ez);
                Vec3d eyes   = mc.player.getEyePos();
                RenderUtil.drawLine3D(matrices, eyes, center, r, g, b, 0.7f, 1.0f);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Returns the colour setting for this entity, or null if it should be skipped. */
    private ColorSetting resolveColor(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            if (!players.isEnabled()) return null;
            String name = player.getGameProfile().getName();
            if (GhostClient.getInstance().getFriendManager().isFriend(name)) return friendColor;
            return playerColor;
        }
        if (entity instanceof AnimalEntity) {
            return animals.isEnabled() ? animalColor : null;
        }
        if (entity instanceof MobEntity) {
            return mobs.isEnabled() ? mobColor : null;
        }
        if (entity instanceof ItemEntity) {
            return items.isEnabled() ? animalColor : null;
        }
        if (entity instanceof BoatEntity || entity instanceof MinecartEntity) {
            return vehicles.isEnabled() ? mobColor : null;
        }
        return null;
    }

    /**
     * Draws only the 8 corner L-brackets of a box (instead of the full wireframe).
     * Each edge contributes 1/4 of its length as a visible corner bracket.
     */
    private void drawCornerBox(MatrixStack matrices, Box box, float r, float g, float b,
                                float alpha, float lineWidth) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d  camPos = camera.getPos();

        double x1 = box.minX - camPos.x, x2 = box.maxX - camPos.x;
        double y1 = box.minY - camPos.y, y2 = box.maxY - camPos.y;
        double z1 = box.minZ - camPos.z, z2 = box.maxZ - camPos.z;

        double lx = (x2 - x1) * 0.25;
        double ly = (y2 - y1) * 0.25;
        double lz = (z2 - z1) * 0.25;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        RenderSystem.lineWidth(lineWidth);

        matrices.push();
        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        Matrix4f m = matrices.peek().getPositionMatrix();
        org.joml.Matrix3f nm = matrices.peek().getNormalMatrix();

        // Helper lambdas via a small emit call
        // Each of the 8 corners gets 3 L-segments
        emitCorner(buf, m, nm, x1, y1, z1,  lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x1, y1, z1,  0,   ly,  0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x1, y1, z1,  0,   0,   lz, r, g, b, alpha);

        emitCorner(buf, m, nm, x2, y1, z1,  -lx, 0,   0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x2, y1, z1,  0,   ly,  0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x2, y1, z1,  0,   0,   lz, r, g, b, alpha);

        emitCorner(buf, m, nm, x1, y2, z1,  lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x1, y2, z1,  0,   -ly, 0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x1, y2, z1,  0,   0,   lz, r, g, b, alpha);

        emitCorner(buf, m, nm, x2, y2, z1,  -lx, 0,   0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x2, y2, z1,  0,   -ly, 0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x2, y2, z1,  0,   0,   lz, r, g, b, alpha);

        emitCorner(buf, m, nm, x1, y1, z2,  lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x1, y1, z2,  0,   ly,  0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x1, y1, z2,  0,   0,   -lz,r, g, b, alpha);

        emitCorner(buf, m, nm, x2, y1, z2,  -lx, 0,   0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x2, y1, z2,  0,   ly,  0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x2, y1, z2,  0,   0,   -lz,r, g, b, alpha);

        emitCorner(buf, m, nm, x1, y2, z2,  lx,  0,   0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x1, y2, z2,  0,   -ly, 0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x1, y2, z2,  0,   0,   -lz,r, g, b, alpha);

        emitCorner(buf, m, nm, x2, y2, z2,  -lx, 0,   0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x2, y2, z2,  0,   -ly, 0,  r, g, b, alpha);
        emitCorner(buf, m, nm, x2, y2, z2,  0,   0,   -lz,r, g, b, alpha);

        Tessellator.getInstance().draw();
        matrices.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    private void emitCorner(BufferBuilder buf, Matrix4f m, org.joml.Matrix3f nm,
                             double ox, double oy, double oz,
                             double dx, double dy, double dz,
                             float r, float g, float b, float a) {
        float ex = (float)(ox + dx), ey = (float)(oy + dy), ez = (float)(oz + dz);
        float len = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (len == 0) return;
        float nx = (float)(dx / len), ny = (float)(dy / len), nz = (float)(dz / len);
        buf.vertex(m, (float)ox, (float)oy, (float)oz).color(r,g,b,a).normal(nm,nx,ny,nz).next();
        buf.vertex(m, ex, ey, ez)                      .color(r,g,b,a).normal(nm,nx,ny,nz).next();
    }

    /**
     * Projects the 3-D bounding box to 2-D screen coordinates and draws a
     * screen-space rectangle overlay.  Falls back gracefully if projection fails.
     */
    private void draw2DESP(MatrixStack matrices, Box box, float r, float g, float b,
                            float tickDelta) {
        if (mc.gameRenderer == null) return;

        // Collect the 8 corners of the box and project each to screen space.
        double[] worldCorners = {
            box.minX, box.minY, box.minZ,
            box.maxX, box.minY, box.minZ,
            box.minX, box.maxY, box.minZ,
            box.maxX, box.maxY, box.minZ,
            box.minX, box.minY, box.maxZ,
            box.maxX, box.minY, box.maxZ,
            box.minX, box.maxY, box.maxZ,
            box.maxX, box.maxY, box.maxZ,
        };

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        double minSX = Double.MAX_VALUE, minSY = Double.MAX_VALUE;
        double maxSX = Double.MIN_VALUE, maxSY = Double.MIN_VALUE;
        boolean anyVisible = false;

        // Use the camera + projection matrices to project world points to NDC
        net.minecraft.client.render.Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        org.joml.Matrix4f proj = RenderSystem.getProjectionMatrix();
        // Build model-view from camera rotation
        org.joml.Matrix4f mv = new org.joml.Matrix4f();
        mv.rotate((float)Math.toRadians(camera.getPitch()), 1, 0, 0);
        mv.rotate((float)Math.toRadians(camera.getYaw() + 180f), 0, 1, 0);

        org.joml.Matrix4f mvp = new org.joml.Matrix4f(proj).mul(mv);

        for (int i = 0; i < worldCorners.length; i += 3) {
            double wx = worldCorners[i]   - camPos.x;
            double wy = worldCorners[i+1] - camPos.y;
            double wz = worldCorners[i+2] - camPos.z;

            org.joml.Vector4f clip = new org.joml.Vector4f(
                    (float)wx, (float)wy, (float)wz, 1.0f);
            mvp.transform(clip);

            if (clip.w <= 0) continue;  // behind camera

            float ndcX = clip.x / clip.w;
            float ndcY = clip.y / clip.w;

            double sx = (ndcX + 1.0) * 0.5 * screenW;
            double sy = (1.0 - ndcY) * 0.5 * screenH;

            if (sx < minSX) minSX = sx;
            if (sy < minSY) minSY = sy;
            if (sx > maxSX) maxSX = sx;
            if (sy > maxSY) maxSY = sy;
            anyVisible = true;
        }

        if (!anyVisible) return;

        // Clamp to screen bounds
        minSX = Math.max(0, minSX);
        minSY = Math.max(0, minSY);
        maxSX = Math.min(screenW, maxSX);
        maxSY = Math.min(screenH, maxSY);

        if (maxSX <= minSX || maxSY <= minSY) return;

        // Draw 2D rect using Tessellator in screen space via a dedicated matrix
        MatrixStack screen = new MatrixStack();
        screen.push();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Outline rect
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf2 = tess.getBuffer();
        buf2.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        Matrix4f sm = screen.peek().getPositionMatrix();
        buf2.vertex(sm,(float)minSX,(float)minSY,0).color(r,g,b,0.9f).next();
        buf2.vertex(sm,(float)maxSX,(float)minSY,0).color(r,g,b,0.9f).next();
        buf2.vertex(sm,(float)maxSX,(float)maxSY,0).color(r,g,b,0.9f).next();
        buf2.vertex(sm,(float)minSX,(float)maxSY,0).color(r,g,b,0.9f).next();
        buf2.vertex(sm,(float)minSX,(float)minSY,0).color(r,g,b,0.9f).next();
        tess.draw();

        screen.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
