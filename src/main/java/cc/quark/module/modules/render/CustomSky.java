package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class CustomSky extends Module {

    private final BoolSetting rainbow   = register(new BoolSetting("Rainbow",   "Cycle through rainbow sky colors", false));
    private final ColorSetting dayColor = register(new ColorSetting("DayColor",   "Sky overlay color during day",   0xFF87CEEB));
    private final ColorSetting nightColor = register(new ColorSetting("NightColor", "Sky overlay color during night", 0xFF000033));

    private float rainbowHue = 0f;

    public CustomSky() {
        super("CustomSky", "Overlays a custom color on the sky for aesthetic effects", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        float timeOfDay = mc.world.getSkyAngle(event.getTickDelta());
        boolean isNight = timeOfDay > 0.25f && timeOfDay < 0.75f;

        float r, g, b, a;
        if (rainbow.isEnabled()) {
            rainbowHue = (rainbowHue + 0.005f) % 1.0f;
            int rgb = java.awt.Color.HSBtoRGB(rainbowHue, 0.7f, 0.9f);
            r = ((rgb >> 16) & 0xFF) / 255f;
            g = ((rgb >> 8) & 0xFF) / 255f;
            b = (rgb & 0xFF) / 255f;
            a = 0.4f;
        } else {
            ColorSetting chosenColor = isNight ? nightColor : dayColor;
            r = chosenColor.getRedF();
            g = chosenColor.getGreenF();
            b = chosenColor.getBlueF();
            a = chosenColor.getAlphaF() * 0.5f;
        }

        // Draw a large dome-like quad in the sky direction
        double dist = 100.0;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        matrices.push();
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f mat = entry.getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();

        // Sky dome overlay as a large horizontal quad far above the player
        double skyY = dist;
        BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buf.vertex(mat, (float)-dist, (float)skyY, (float)-dist).color(r, g, b, a);
        buf.vertex(mat, (float) dist, (float)skyY, (float)-dist).color(r, g, b, a);
        buf.vertex(mat, (float) dist, (float)skyY, (float) dist).color(r, g, b, a);
        buf.vertex(mat, (float)-dist, (float)skyY, (float) dist).color(r, g, b, a);
        BufferRenderer.drawWithGlobalProgram(buf.end());

        matrices.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
