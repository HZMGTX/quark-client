package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

/**
 * ChunkBoundary - draws vertical lines at chunk boundaries around the player.
 */
public class ChunkBoundary extends Module {

    private final BoolSetting filled = register(new BoolSetting(
            "Filled", "Fill chunks with a semi-transparent color", false));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Chunk boundary line color", 0xFF00FFFF));

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Number of chunks around player to render", 2, 1, 8));

    public ChunkBoundary() {
        super("ChunkBoundary", "Shows chunk boundaries as colored lines in the world", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();
        float fa = a * 0.1f;

        ChunkPos playerChunk = mc.player.getChunkPos();
        int rad = radius.get();
        int minY = mc.world.getBottomY();
        int maxY = mc.world.getTopY();

        for (int cx = playerChunk.x - rad; cx <= playerChunk.x + rad + 1; cx++) {
            for (int cz = playerChunk.z - rad; cz <= playerChunk.z + rad + 1; cz++) {
                // West edge of chunk (cx, cz): x = cx * 16
                int worldX = cx * 16;
                int worldZ = cz * 16;

                // Draw vertical line at x boundary (west edge of chunk column)
                Vec3d bottom1 = new Vec3d(worldX, minY, worldZ);
                Vec3d top1    = new Vec3d(worldX, maxY, worldZ);
                RenderUtil.drawLine3D(matrices, bottom1, top1, r, g, b, a, 1.0f);

                // Draw vertical line at z boundary
                Vec3d bottom2 = new Vec3d(worldX + 16, minY, worldZ);
                Vec3d top2    = new Vec3d(worldX + 16, maxY, worldZ);
                RenderUtil.drawLine3D(matrices, bottom2, top2, r, g, b, a, 1.0f);

                if (filled.isEnabled()) {
                    // Draw horizontal outline at player Y level
                    double py = mc.player.getY();
                    Vec3d c1 = new Vec3d(worldX,      py, worldZ);
                    Vec3d c2 = new Vec3d(worldX + 16, py, worldZ);
                    Vec3d c3 = new Vec3d(worldX + 16, py, worldZ + 16);
                    Vec3d c4 = new Vec3d(worldX,      py, worldZ + 16);
                    RenderUtil.drawLine3D(matrices, c1, c2, r, g, b, fa, 1.0f);
                    RenderUtil.drawLine3D(matrices, c2, c3, r, g, b, fa, 1.0f);
                    RenderUtil.drawLine3D(matrices, c3, c4, r, g, b, fa, 1.0f);
                    RenderUtil.drawLine3D(matrices, c4, c1, r, g, b, fa, 1.0f);
                }
            }
        }
    }
}
