package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ChunkBorder extends Module {

    private final BoolSetting showAll = register(new BoolSetting(
            "Show All", "Show borders for all nearby chunks (8x8 grid)", false));
    private final IntSetting alpha = register(new IntSetting(
            "Alpha", "Line opacity 0-255", 180, 20, 255));

    public ChunkBorder() {
        super("ChunkBorder", "Renders visible chunk boundaries as colored lines", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int chunkX = playerPos.getX() >> 4;
        int chunkZ = playerPos.getZ() >> 4;
        double py = mc.player.getY();

        int range = showAll.isEnabled() ? 4 : 1;
        float a = alpha.get() / 255f;
        float r = 0f, g = 1f, b = 0.4f;

        MatrixStack matrices = event.getMatrixStack();

        for (int cx = chunkX - range; cx <= chunkX + range; cx++) {
            for (int cz = chunkZ - range; cz <= chunkZ + range; cz++) {
                double x0 = cx << 4;
                double z0 = cz << 4;
                double x1 = x0 + 16;
                double z1 = z0 + 16;
                double y0 = py - 4;
                double y1 = py + 4;

                RenderUtil.drawLine3D(matrices, new Vec3d(x0, y0, z0), new Vec3d(x0, y1, z0), r, g, b, a, 1.5f);
                RenderUtil.drawLine3D(matrices, new Vec3d(x1, y0, z0), new Vec3d(x1, y1, z0), r, g, b, a, 1.5f);
                RenderUtil.drawLine3D(matrices, new Vec3d(x0, y0, z1), new Vec3d(x0, y1, z1), r, g, b, a, 1.5f);
                RenderUtil.drawLine3D(matrices, new Vec3d(x1, y0, z1), new Vec3d(x1, y1, z1), r, g, b, a, 1.5f);
            }
        }
    }
}
