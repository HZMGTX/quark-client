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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * ChunkBorderESP - Highlights chunk borders with colored 3D outlines.
 *
 * Draws vertical line segments at the four corners of every chunk within the
 * configured render radius, centred on the player's current chunk.
 */
public class ChunkBorderESP extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Chunk radius to draw (0 = current chunk only)", 2, 0, 8));

    private final IntSetting height = register(new IntSetting(
            "Height", "Height of the corner lines above/below player", 6, 1, 64));

    private final ColorSetting borderColor = register(new ColorSetting(
            "Color", "Chunk border line color (ARGB)", 0xCC00FF88));

    private final BoolSetting showCornerDots = register(new BoolSetting(
            "Corner Dots", "Draw a filled pixel at each chunk corner", true));

    private final BoolSetting showLabels = register(new BoolSetting(
            "Labels", "Show chunk X/Z coordinate label above each border", false));

    public ChunkBorderESP() {
        super("ChunkBorderESP", "Highlights chunk borders with colored outlines in 3D", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int chunkX = playerPos.getX() >> 4;
        int chunkZ = playerPos.getZ() >> 4;
        double py  = mc.player.getY();
        int rad    = radius.get();
        int ht     = height.get();

        int argb   = borderColor.get();
        float a    = ((argb >> 24) & 0xFF) / 255f;
        float r    = ((argb >> 16) & 0xFF) / 255f;
        float g    = ((argb >> 8)  & 0xFF) / 255f;
        float b    = (argb         & 0xFF) / 255f;

        MatrixStack matrices = event.getMatrixStack();

        for (int cx = chunkX - rad; cx <= chunkX + rad; cx++) {
            for (int cz = chunkZ - rad; cz <= chunkZ + rad; cz++) {
                double x0 = cx << 4;
                double z0 = cz << 4;
                double x1 = x0 + 16;
                double z1 = z0 + 16;
                double y0 = py - ht;
                double y1 = py + ht;

                // Draw vertical lines at all four corners
                RenderUtil.drawLine3D(matrices, new Vec3d(x0, y0, z0), new Vec3d(x0, y1, z0), r, g, b, a, 1.5f);
                RenderUtil.drawLine3D(matrices, new Vec3d(x1, y0, z0), new Vec3d(x1, y1, z0), r, g, b, a, 1.5f);
                RenderUtil.drawLine3D(matrices, new Vec3d(x0, y0, z1), new Vec3d(x0, y1, z1), r, g, b, a, 1.5f);
                RenderUtil.drawLine3D(matrices, new Vec3d(x1, y0, z1), new Vec3d(x1, y1, z1), r, g, b, a, 1.5f);

                // Top and bottom horizontal outlines at player level
                RenderUtil.drawLine3D(matrices, new Vec3d(x0, py, z0), new Vec3d(x1, py, z0), r, g, b, a * 0.6f, 1.0f);
                RenderUtil.drawLine3D(matrices, new Vec3d(x1, py, z0), new Vec3d(x1, py, z1), r, g, b, a * 0.6f, 1.0f);
                RenderUtil.drawLine3D(matrices, new Vec3d(x1, py, z1), new Vec3d(x0, py, z1), r, g, b, a * 0.6f, 1.0f);
                RenderUtil.drawLine3D(matrices, new Vec3d(x0, py, z1), new Vec3d(x0, py, z0), r, g, b, a * 0.6f, 1.0f);
            }
        }
    }
}
