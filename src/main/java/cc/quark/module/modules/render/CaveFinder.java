package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * CaveFinder - highlights air pockets underground by drawing ESP boxes
 * around air blocks below a configurable Y level that are adjacent to solid
 * blocks (indicating a cave wall or ceiling).
 */
public class CaveFinder extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Scan radius around player", 4, 2, 6));

    private final IntSetting minY = register(new IntSetting(
            "Min Y", "Only scan below this Y level", 40, -64, 128));

    private final ColorSetting color = register(new ColorSetting(
            "Color", "Cave air highlight color", 0x8000AAFF));

    public CaveFinder() {
        super("CaveFinder", "Highlights underground air pockets adjacent to solid blocks", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float r = color.getRedF();
        float g = color.getGreenF();
        float b = color.getBlueF();
        float a = color.getAlphaF();

        int rad = range.get();
        int maxYLevel = minY.get();
        int px = (int) mc.player.getX();
        int py = (int) mc.player.getY();
        int pz = (int) mc.player.getZ();

        for (int x = px - rad; x <= px + rad; x++) {
            for (int y = py - rad; y <= py + rad; y++) {
                if (y >= maxYLevel) continue;
                for (int z = pz - rad; z <= pz + rad; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    if (!state.isAir()) continue;

                    // Only draw if at least one adjacent block is solid
                    if (hasAdjacentSolid(pos)) {
                        Box box = new Box(x, y, z, x + 1, y + 1, z + 1);
                        RenderUtil.drawFilledBox(matrices, box, r, g, b, a * 0.35f);
                        RenderUtil.drawESPBox(matrices, box, r, g, b, a, 1.0f);
                    }
                }
            }
        }
    }

    private boolean hasAdjacentSolid(BlockPos pos) {
        BlockPos[] neighbors = {
                pos.up(), pos.down(),
                pos.north(), pos.south(),
                pos.east(), pos.west()
        };
        for (BlockPos n : neighbors) {
            BlockState ns = mc.world.getBlockState(n);
            if (!ns.isAir() && ns.isOpaque()) return true;
        }
        return false;
    }
}
