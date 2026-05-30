package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

public class LightESP extends Module {

    private final IntSetting range     = register(new IntSetting("Range",     "Block scan radius",          16, 4,  32));
    private final IntSetting threshold = register(new IntSetting("Threshold", "Light level danger threshold", 8, 1, 15));

    public LightESP() {
        super("LightESP", "Highlights blocks with dangerously low light levels where mobs can spawn", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();
        int thresh = threshold.get();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        matrices.push();
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f mat = entry.getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -4; dy <= 4; dy++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    if (!mc.world.getBlockState(pos).isAir()) continue;
                    if (!mc.world.getBlockState(pos.down()).isSolidBlock(mc.world, pos.down())) continue;

                    int lightLevel = mc.world.getLightLevel(LightType.BLOCK, pos);
                    if (lightLevel >= thresh) continue;

                    float red, green, blue;
                    if (lightLevel < thresh) {
                        if (lightLevel <= thresh - 4) {
                            // Red: very dangerous
                            red = 1.0f; green = 0.0f; blue = 0.0f;
                        } else {
                            // Yellow: borderline
                            red = 1.0f; green = 1.0f; blue = 0.0f;
                        }
                    } else {
                        continue;
                    }

                    double x1 = pos.getX() - camPos.x;
                    double y1 = pos.getY() - camPos.y;
                    double z1 = pos.getZ() - camPos.z;
                    double x2 = x1 + 1.0;
                    double z2 = z1 + 1.0;

                    float fr = red, fg = green, fb = blue;
                    BufferBuilder buf = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
                    buf.vertex(mat, (float)x1, (float)y1, (float)z1).color(fr, fg, fb, 0.45f);
                    buf.vertex(mat, (float)x2, (float)y1, (float)z1).color(fr, fg, fb, 0.45f);
                    buf.vertex(mat, (float)x2, (float)y1, (float)z2).color(fr, fg, fb, 0.45f);
                    buf.vertex(mat, (float)x1, (float)y1, (float)z2).color(fr, fg, fb, 0.45f);
                    BufferRenderer.drawWithGlobalProgram(buf.end());
                }
            }
        }

        matrices.pop();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
