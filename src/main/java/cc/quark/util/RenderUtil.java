package cc.quark.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Utility methods for 3D and 2D rendering (ESP boxes, tracer lines, etc.).
 */
public class RenderUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    /**
     * Draws a 3D axis-aligned bounding box outline (ESP box) in the world.
     *
     * @param matrices  the current matrix stack
     * @param box       the bounding box to draw
     * @param r         red component 0-1
     * @param g         green component 0-1
     * @param b         blue component 0-1
     * @param a         alpha component 0-1
     * @param lineWidth line width in pixels
     */
    public static void drawESPBox(MatrixStack matrices, Box box, float r, float g, float b, float a, float lineWidth) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        double x1 = box.minX - camPos.x;
        double y1 = box.minY - camPos.y;
        double z1 = box.minZ - camPos.z;
        double x2 = box.maxX - camPos.x;
        double y2 = box.maxY - camPos.y;
        double z2 = box.maxZ - camPos.z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        RenderSystem.lineWidth(lineWidth);

        matrices.push();

        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        org.joml.Matrix3f normalMatrix = matrices.peek().getNormalMatrix();

        // Bottom face
        drawLine(buf, matrix, normalMatrix, x1, y1, z1, x2, y1, z1, r, g, b, a);
        drawLine(buf, matrix, normalMatrix, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(buf, matrix, normalMatrix, x2, y1, z2, x1, y1, z2, r, g, b, a);
        drawLine(buf, matrix, normalMatrix, x1, y1, z2, x1, y1, z1, r, g, b, a);
        // Top face
        drawLine(buf, matrix, normalMatrix, x1, y2, z1, x2, y2, z1, r, g, b, a);
        drawLine(buf, matrix, normalMatrix, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(buf, matrix, normalMatrix, x2, y2, z2, x1, y2, z2, r, g, b, a);
        drawLine(buf, matrix, normalMatrix, x1, y2, z2, x1, y2, z1, r, g, b, a);
        // Verticals
        drawLine(buf, matrix, normalMatrix, x1, y1, z1, x1, y2, z1, r, g, b, a);
        drawLine(buf, matrix, normalMatrix, x2, y1, z1, x2, y2, z1, r, g, b, a);
        drawLine(buf, matrix, normalMatrix, x2, y1, z2, x2, y2, z2, r, g, b, a);
        drawLine(buf, matrix, normalMatrix, x1, y1, z2, x1, y2, z2, r, g, b, a);

        Tessellator.getInstance().draw();

        matrices.pop();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    /**
     * Draws a filled 3D box.
     */
    public static void drawFilledBox(MatrixStack matrices, Box box, float r, float g, float b, float a) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        double x1 = box.minX - camPos.x;
        double y1 = box.minY - camPos.y;
        double z1 = box.minZ - camPos.z;
        double x2 = box.maxX - camPos.x;
        double y2 = box.maxY - camPos.y;
        double z2 = box.maxZ - camPos.z;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        matrices.push();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        Matrix4f m = matrices.peek().getPositionMatrix();

        // All 6 faces
        // Bottom
        buf.vertex(m, (float)x1, (float)y1, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y1, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y1, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y1, (float)z2).color(r,g,b,a).next();
        // Top
        buf.vertex(m, (float)x1, (float)y2, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y2, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y2, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y2, (float)z1).color(r,g,b,a).next();
        // North
        buf.vertex(m, (float)x1, (float)y1, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y2, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y2, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y1, (float)z1).color(r,g,b,a).next();
        // South
        buf.vertex(m, (float)x1, (float)y1, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y1, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y2, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y2, (float)z2).color(r,g,b,a).next();
        // West
        buf.vertex(m, (float)x1, (float)y1, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y1, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y2, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y2, (float)z1).color(r,g,b,a).next();
        // East
        buf.vertex(m, (float)x2, (float)y1, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y2, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y2, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y1, (float)z2).color(r,g,b,a).next();

        tess.draw();
        matrices.pop();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /**
     * Draws a 3D line between two world positions.
     */
    public static void drawLine3D(MatrixStack matrices, Vec3d from, Vec3d to, float r, float g, float b, float a, float width) {
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader);
        RenderSystem.lineWidth(width);

        matrices.push();

        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        org.joml.Matrix3f normalMatrix = matrices.peek().getNormalMatrix();

        double x1 = from.x - camPos.x;
        double y1 = from.y - camPos.y;
        double z1 = from.z - camPos.z;
        double x2 = to.x - camPos.x;
        double y2 = to.y - camPos.y;
        double z2 = to.z - camPos.z;

        drawLine(buf, matrix, normalMatrix, x1, y1, z1, x2, y2, z2, r, g, b, a);

        Tessellator.getInstance().draw();
        matrices.pop();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    private static void drawLine(BufferBuilder buf, Matrix4f matrix, org.joml.Matrix3f normalMatrix,
                                 double x1, double y1, double z1,
                                 double x2, double y2, double z2,
                                 float r, float g, float b, float a) {
        float dx = (float)(x2 - x1);
        float dy = (float)(y2 - y1);
        float dz = (float)(z2 - z1);
        float len = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (len == 0) return;
        float nx = dx / len;
        float ny = dy / len;
        float nz = dz / len;

        buf.vertex(matrix, (float)x1, (float)y1, (float)z1).color(r, g, b, a).normal(normalMatrix, nx, ny, nz).next();
        buf.vertex(matrix, (float)x2, (float)y2, (float)z2).color(r, g, b, a).normal(normalMatrix, nx, ny, nz).next();
    }

    /**
     * Projects a 3D world position to 2D screen coordinates.
     * Returns null if the position is behind the camera.
     */
    public static double[] project(Vec3d worldPos) {
        return WorldRenderer.getArrowDirection(mc.getEntityRenderDispatcher(), worldPos.x, worldPos.y, worldPos.z);
    }

    /**
     * Simple color helper: packs RGBA into an int.
     */
    public static int toARGB(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Rainbow color based on time + offset hue.
     */
    public static int rainbowColor(float offset) {
        float hue = (System.currentTimeMillis() % 2000L) / 2000.0f + offset;
        hue %= 1.0f;
        int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
        return 0xFF000000 | rgb;
    }
}
