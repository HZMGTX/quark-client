package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/**
 * HorizonESP — renders a circular or box boundary ring at the horizon around the player,
 * showing the visible ESP radius boundary in 3D space.
 */
public class HorizonESP extends Module {

    private final IntSetting    radius    = register(new IntSetting(   "Radius",    "Horizon ring radius in blocks",   64, 8, 256));
    private final ColorSetting  ringColor = register(new ColorSetting( "Color",     "Ring color",                      0x8800AAFF));
    private final DoubleSetting lineWidth = register(new DoubleSetting("Line Width","Line thickness",                  1.5, 0.5, 4.0));
    private final IntSetting    segments  = register(new IntSetting(   "Segments",  "Circle smoothness (segments)",    64, 16, 128));
    private final DoubleSetting yOffset   = register(new DoubleSetting("Y Offset",  "Vertical offset from player eye", 0.0, -32.0, 32.0));

    public HorizonESP() {
        super("HorizonESP", "Shows the ESP horizon boundary ring around the player in 3D", Category.RENDER);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getCamera();
        net.minecraft.util.math.Vec3d camPos = camera.getPos();

        double cx = mc.player.getX() - camPos.x;
        double cy = mc.player.getEyeY() + yOffset.get() - camPos.y;
        double cz = mc.player.getZ() - camPos.z;

        float r = ringColor.getRedF();
        float g = ringColor.getGreenF();
        float b = ringColor.getBlueF();
        float a = ringColor.getAlphaF();
        double rad = radius.get();
        int segs = segments.get();

        MatrixStack matrices = event.getMatrixStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth((float) lineWidth.get());

        matrices.push();
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f mat = entry.getPositionMatrix();
        org.joml.Matrix3f norm = entry.getNormalMatrix();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);

        for (int i = 0; i < segs; i++) {
            double angle1 = (i / (double) segs) * 2 * Math.PI;
            double angle2 = ((i + 1) / (double) segs) * 2 * Math.PI;

            float x1 = (float) (cx + Math.cos(angle1) * rad);
            float z1 = (float) (cz + Math.sin(angle1) * rad);
            float x2 = (float) (cx + Math.cos(angle2) * rad);
            float z2 = (float) (cz + Math.sin(angle2) * rad);

            float ny = 0f;
            float nx1 = (float) Math.cos(angle1);
            float nz1 = (float) Math.sin(angle1);
            float nx2 = (float) Math.cos(angle2);
            float nz2 = (float) Math.sin(angle2);

            buf.vertex(mat, x1, (float) cy, z1).color(r, g, b, a).normal(norm, nx1, ny, nz1);
            buf.vertex(mat, x2, (float) cy, z2).color(r, g, b, a).normal(norm, nx2, ny, nz2);
        }

        BufferRenderer.drawWithGlobalProgram(buf.end());
        matrices.pop();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
