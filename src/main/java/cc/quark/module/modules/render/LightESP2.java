package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

/**
 * LightESP2 — highlights blocks with dangerous (mob-spawn-enabling) light
 * levels using configurable color tiers and rendering modes.
 */
public class LightESP2 extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Block scan radius around player", 16, 4, 32));

    private final IntSetting dangerThreshold = register(new IntSetting(
            "Danger Threshold", "Light levels at or below this are shown as red", 4, 0, 15));

    private final IntSetting warnThreshold = register(new IntSetting(
            "Warn Threshold", "Light levels at or below this are shown as yellow", 8, 0, 15));

    private final ColorSetting dangerColor = register(new ColorSetting(
            "Danger Color", "Color for very dark spots", 0xCCFF2222));

    private final ColorSetting warnColor = register(new ColorSetting(
            "Warn Color", "Color for borderline light spots", 0xCCFFFF00));

    private final ModeSetting checkMode = register(new ModeSetting(
            "Light Type", "Which light type to check",
            "Block", "Block", "Sky", "Combined"));

    private final BoolSetting requireSolid = register(new BoolSetting(
            "Require Solid Floor", "Only mark spots where floor block is solid", true));

    public LightESP2() {
        super("LightESP2", "Highlights blocks with dangerous light levels", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();
        int dangerT = dangerThreshold.get();
        int warnT = warnThreshold.get();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        matrices.push();
        Matrix4f mat = matrices.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -4; dy <= 4; dy++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    if (!mc.world.getBlockState(pos).isAir()) continue;

                    BlockPos below = pos.down();
                    if (requireSolid.isEnabled()
                            && !mc.world.getBlockState(below).isSolidBlock(mc.world, below)) continue;

                    int light = switch (checkMode.get()) {
                        case "Sky"      -> mc.world.getLightLevel(LightType.SKY, pos);
                        case "Combined" -> Math.max(mc.world.getLightLevel(LightType.BLOCK, pos),
                                mc.world.getLightLevel(LightType.SKY, pos));
                        default         -> mc.world.getLightLevel(LightType.BLOCK, pos);
                    };

                    if (light > warnT) continue;

                    boolean danger = light <= dangerT;
                    float fr = danger ? dangerColor.getRedF() : warnColor.getRedF();
                    float fg = danger ? dangerColor.getGreenF() : warnColor.getGreenF();
                    float fb = danger ? dangerColor.getBlueF() : warnColor.getBlueF();
                    float fa = danger ? dangerColor.getAlphaF() : warnColor.getAlphaF();

                    double x1 = pos.getX() - camPos.x;
                    double y1 = pos.getY() - camPos.y;
                    double z1 = pos.getZ() - camPos.z;
                    double x2 = x1 + 1.0;
                    double z2 = z1 + 1.0;

                    BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    buf.vertex(mat, (float)x1, (float)y1, (float)z1).color(fr, fg, fb, fa);
                    buf.vertex(mat, (float)x2, (float)y1, (float)z1).color(fr, fg, fb, fa);
                    buf.vertex(mat, (float)x2, (float)y1, (float)z2).color(fr, fg, fb, fa);
                    buf.vertex(mat, (float)x1, (float)y1, (float)z2).color(fr, fg, fb, fa);
                    BufferRenderer.drawWithGlobalProgram(buf.end());
                }
            }
        }

        matrices.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
