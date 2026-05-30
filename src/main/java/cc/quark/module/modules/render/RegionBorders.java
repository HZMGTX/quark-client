package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class RegionBorders extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Number of regions around player to render", 2, 1, 5));
    private final ColorSetting color = register(new ColorSetting("Color", "Region border line color", 0xFFFF0000));

    // Minecraft region files are 512x512 blocks (32 chunks * 16 blocks)
    private static final int REGION_SIZE = 512;

    public RegionBorders() {
        super("RegionBorders", "Shows 512x512 region file boundaries as vertical lines", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();

        int playerRegionX = Math.floorDiv((int) mc.player.getX(), REGION_SIZE);
        int playerRegionZ = Math.floorDiv((int) mc.player.getZ(), REGION_SIZE);
        int rad = range.get();

        int minY = mc.world.getBottomY();
        int maxY = mc.world.getTopY();

        for (int rx = playerRegionX - rad; rx <= playerRegionX + rad + 1; rx++) {
            for (int rz = playerRegionZ - rad; rz <= playerRegionZ + rad + 1; rz++) {
                int worldX = rx * REGION_SIZE;
                int worldZ = rz * REGION_SIZE;

                // Vertical line at west/north edge of region
                Vec3d bottomWest = new Vec3d(worldX, minY, worldZ);
                Vec3d topWest    = new Vec3d(worldX, maxY, worldZ);
                RenderUtil.drawLine3D(matrices, bottomWest, topWest, r, g, b, a, 2.0f);

                // Vertical line at east side
                Vec3d bottomEast = new Vec3d(worldX + REGION_SIZE, minY, worldZ);
                Vec3d topEast    = new Vec3d(worldX + REGION_SIZE, maxY, worldZ);
                RenderUtil.drawLine3D(matrices, bottomEast, topEast, r, g, b, a, 2.0f);

                // Horizontal outline at player Y (region corners)
                double py = mc.player.getY();
                Vec3d c1 = new Vec3d(worldX,              py, worldZ);
                Vec3d c2 = new Vec3d(worldX + REGION_SIZE, py, worldZ);
                Vec3d c3 = new Vec3d(worldX + REGION_SIZE, py, worldZ + REGION_SIZE);
                Vec3d c4 = new Vec3d(worldX,              py, worldZ + REGION_SIZE);
                RenderUtil.drawLine3D(matrices, c1, c2, r, g, b, a * 0.5f, 1.5f);
                RenderUtil.drawLine3D(matrices, c2, c3, r, g, b, a * 0.5f, 1.5f);
                RenderUtil.drawLine3D(matrices, c3, c4, r, g, b, a * 0.5f, 1.5f);
                RenderUtil.drawLine3D(matrices, c4, c1, r, g, b, a * 0.5f, 1.5f);
            }
        }
    }
}
