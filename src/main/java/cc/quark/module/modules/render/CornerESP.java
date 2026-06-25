package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class CornerESP extends Module {

    private final BoolSetting onlyAir = register(new BoolSetting("OnlyAir", "Only mark corners where adjacent blocks are air", true));

    public CornerESP() {
        super("CornerESP", "Renders ESP markers at block corners for precise positioning", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        BlockPos playerPos = mc.player.getBlockPos();
        int radius = 5;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(pos).isAir()) continue;
                    if (onlyAir.isEnabled()) {
                        boolean anyAirNeighbor = mc.world.getBlockState(pos.east()).isAir()
                                || mc.world.getBlockState(pos.west()).isAir()
                                || mc.world.getBlockState(pos.north()).isAir()
                                || mc.world.getBlockState(pos.south()).isAir();
                        if (!anyAirNeighbor) continue;
                    }
                    double bx = pos.getX(), by = pos.getY(), bz = pos.getZ();
                    double s = 0.06;
                    for (int cx = 0; cx <= 1; cx++) {
                        for (int cz = 0; cz <= 1; cz++) {
                            Box corner = new Box(bx + cx - s, by + 1 - s, bz + cz - s,
                                    bx + cx + s, by + 1 + s, bz + cz + s);
                            RenderUtil.drawFilledBox(m, corner, 0.9f, 0.9f, 0.1f, 0.7f);
                        }
                    }
                }
            }
        }
    }
}
