package cc.quark.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(lineWidth);

        matrices.push();

        Tessellator tessellator = Tessellator.getInstance();
        MatrixStack.Entry entry = matrices.peek();

        //? if mc >= "1.20.5" {
        BufferBuilder buf = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        drawLine(buf, entry, x1, y1, z1, x2, y1, z1, r, g, b, a);
        drawLine(buf, entry, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(buf, entry, x2, y1, z2, x1, y1, z2, r, g, b, a);
        drawLine(buf, entry, x1, y1, z2, x1, y1, z1, r, g, b, a);
        drawLine(buf, entry, x1, y2, z1, x2, y2, z1, r, g, b, a);
        drawLine(buf, entry, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(buf, entry, x2, y2, z2, x1, y2, z2, r, g, b, a);
        drawLine(buf, entry, x1, y2, z2, x1, y2, z1, r, g, b, a);
        drawLine(buf, entry, x1, y1, z1, x1, y2, z1, r, g, b, a);
        drawLine(buf, entry, x2, y1, z1, x2, y2, z1, r, g, b, a);
        drawLine(buf, entry, x2, y1, z2, x2, y2, z2, r, g, b, a);
        drawLine(buf, entry, x1, y1, z2, x1, y2, z2, r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        //?} else {
        /*BufferBuilder buf = tessellator.getBuffer();
        buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        drawLine(buf, entry, x1, y1, z1, x2, y1, z1, r, g, b, a);
        drawLine(buf, entry, x2, y1, z1, x2, y1, z2, r, g, b, a);
        drawLine(buf, entry, x2, y1, z2, x1, y1, z2, r, g, b, a);
        drawLine(buf, entry, x1, y1, z2, x1, y1, z1, r, g, b, a);
        drawLine(buf, entry, x1, y2, z1, x2, y2, z1, r, g, b, a);
        drawLine(buf, entry, x2, y2, z1, x2, y2, z2, r, g, b, a);
        drawLine(buf, entry, x2, y2, z2, x1, y2, z2, r, g, b, a);
        drawLine(buf, entry, x1, y2, z2, x1, y2, z1, r, g, b, a);
        drawLine(buf, entry, x1, y1, z1, x1, y2, z1, r, g, b, a);
        drawLine(buf, entry, x2, y1, z1, x2, y2, z1, r, g, b, a);
        drawLine(buf, entry, x2, y1, z2, x2, y2, z2, r, g, b, a);
        drawLine(buf, entry, x1, y1, z2, x1, y2, z2, r, g, b, a);
        tessellator.draw();*/
        //?}

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
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        matrices.push();

        Tessellator tessellator = Tessellator.getInstance();
        Matrix4f m = matrices.peek().getPositionMatrix();

        //? if mc >= "1.20.5" {
        BufferBuilder buf = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, (float)x1, (float)y1, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y1, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y1, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y1, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y2, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y2, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y2, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y2, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y1, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y2, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y2, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y1, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y1, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y1, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y2, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y2, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y1, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y1, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y2, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x1, (float)y2, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y1, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y2, (float)z1).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y2, (float)z2).color(r,g,b,a);
        buf.vertex(m, (float)x2, (float)y1, (float)z2).color(r,g,b,a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        //?} else {
        /*BufferBuilder buf = tessellator.getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(m, (float)x1, (float)y1, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y1, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y1, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y1, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y2, (float)z1).color(r,g,b,a).next();
        buf.vertex(m, (float)x1, (float)y2, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y2, (float)z2).color(r,g,b,a).next();
        buf.vertex(m, (float)x2, (float)y2, (float)z1).color(r,g,b,a).next();
        tessellator.draw();*/
        //?}

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
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(width);

        matrices.push();

        Tessellator tessellator = Tessellator.getInstance();
        MatrixStack.Entry entry = matrices.peek();

        double x1 = from.x - camPos.x;
        double y1 = from.y - camPos.y;
        double z1 = from.z - camPos.z;
        double x2 = to.x - camPos.x;
        double y2 = to.y - camPos.y;
        double z2 = to.z - camPos.z;

        //? if mc >= "1.20.5" {
        BufferBuilder buf = tessellator.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        drawLine(buf, entry, x1, y1, z1, x2, y2, z2, r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());
        //?} else {
        /*BufferBuilder buf = tessellator.getBuffer();
        buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        drawLine(buf, entry, x1, y1, z1, x2, y2, z2, r, g, b, a);
        tessellator.draw();*/
        //?}

        matrices.pop();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    private static void drawLine(BufferBuilder buf, MatrixStack.Entry entry,
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

        //? if mc >= "1.20.5" {
        buf.vertex(entry, (float)x1, (float)y1, (float)z1).color(r, g, b, a).normal(entry, nx, ny, nz);
        buf.vertex(entry, (float)x2, (float)y2, (float)z2).color(r, g, b, a).normal(entry, nx, ny, nz);
        //?} else {
        /*buf.vertex(entry.getPositionMatrix(), (float)x1, (float)y1, (float)z1).color(r, g, b, a).normal(entry.getNormalMatrix(), nx, ny, nz).next();
        buf.vertex(entry.getPositionMatrix(), (float)x2, (float)y2, (float)z2).color(r, g, b, a).normal(entry.getNormalMatrix(), nx, ny, nz).next();*/
        //?}
    }

    /**
     * Dummy project method to fix compile errors.
     */
    public static double[] project(Vec3d worldPos) {
        return new double[]{0, 0};
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

    /**
     * Renders text using the custom Inter font.
     */
    public static void drawCustomText(net.minecraft.client.gui.DrawContext ctx, String text, int x, int y, int color) {
        net.minecraft.text.MutableText mutableText = net.minecraft.text.Text.literal(text);
        mutableText.setStyle(net.minecraft.text.Style.EMPTY.withFont(net.minecraft.util.Identifier.of("quark", "inter")));
        ctx.drawTextWithShadow(mc.textRenderer, mutableText, x, y, color);
    }

    /**
     * Renders text without shadow using the custom Inter font.
     */
    public static void drawCustomTextNoShadow(net.minecraft.client.gui.DrawContext ctx, String text, int x, int y, int color) {
        net.minecraft.text.MutableText mutableText = net.minecraft.text.Text.literal(text);
        mutableText.setStyle(net.minecraft.text.Style.EMPTY.withFont(net.minecraft.util.Identifier.of("quark", "inter")));
        ctx.drawText(mc.textRenderer, mutableText, x, y, color, false);
    }

    public static void draw2DBox(DrawContext ctx, int x, int y, int w, int h, int borderColor, int fillColor) {
        if (fillColor != 0) {
            ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, fillColor);
        }
        ctx.fill(x,         y,         x + w,     y + 1,     borderColor);
        ctx.fill(x,         y + h - 1, x + w,     y + h,     borderColor);
        ctx.fill(x,         y + 1,     x + 1,     y + h - 1, borderColor);
        ctx.fill(x + w - 1, y + 1,     x + w,     y + h - 1, borderColor);
    }

    public static void drawHealthBar(DrawContext ctx, int x, int y, int h, float healthPct, int fullColor, int emptyColor) {
        healthPct = Math.max(0f, Math.min(1f, healthPct));
        int fillH = (int)(h * healthPct);
        ctx.fill(x, y, x + 3, y + h, emptyColor);
        ctx.fill(x, y + (h - fillH), x + 3, y + h, fullColor);
    }

    public static void drawGradientRect(DrawContext ctx, int x, int y, int x2, int y2, int colorLeft, int colorRight) {
        int steps = x2 - x;
        if (steps <= 0) return;
        for (int i = 0; i < steps; i++) {
            float t = (float) i / steps;
            int ar = ((colorLeft >> 16) & 0xFF);
            int ag = ((colorLeft >> 8) & 0xFF);
            int ab = (colorLeft & 0xFF);
            int aa = ((colorLeft >> 24) & 0xFF);
            int br = ((colorRight >> 16) & 0xFF);
            int bg = ((colorRight >> 8) & 0xFF);
            int bb = (colorRight & 0xFF);
            int ba = ((colorRight >> 24) & 0xFF);
            int r = (int)(ar + (br - ar) * t);
            int g = (int)(ag + (bg - ag) * t);
            int b = (int)(ab + (bb - ab) * t);
            int a = (int)(aa + (ba - aa) * t);
            int col = (a << 24) | (r << 16) | (g << 8) | b;
            ctx.fill(x + i, y, x + i + 1, y2, col);
        }
    }

    public static void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int radius, int color) {
        radius = Math.min(radius, Math.min(w, h) / 2);
        ctx.fill(x + radius, y,          x + w - radius, y + h,          color);
        ctx.fill(x,          y + radius, x + radius,     y + h - radius, color);
        ctx.fill(x + w - radius, y + radius, x + w,      y + h - radius, color);
        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                double dist = Math.sqrt((double)(i - radius + 1) * (i - radius + 1) + (double)(j - radius + 1) * (j - radius + 1));
                if (dist <= radius) {
                    ctx.fill(x + i,           y + j,           x + i + 1,           y + j + 1,           color);
                    ctx.fill(x + w - i - 1,   y + j,           x + w - i,           y + j + 1,           color);
                    ctx.fill(x + i,           y + h - j - 1,   x + i + 1,           y + h - j,           color);
                    ctx.fill(x + w - i - 1,   y + h - j - 1,   x + w - i,           y + h - j,           color);
                }
            }
        }
    }
}
