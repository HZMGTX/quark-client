package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public class PortalESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Portal ESP color", 0xFF9933FF));
    private final BoolSetting tracers = register(new BoolSetting("Tracers", "Draw tracer lines to portals", false));

    public PortalESP() {
        super("PortalESP", "ESP for nether portal blocks", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF(), a = color.getAlphaF();
        BlockPos playerPos = mc.player.getBlockPos();
        int chunkRadius = 4;
        ChunkPos playerChunk = new ChunkPos(playerPos);
        Vec3d camVec = mc.player.getCameraPosVec(event.getTickDelta());

        for (int cx = playerChunk.x - chunkRadius; cx <= playerChunk.x + chunkRadius; cx++) {
            for (int cz = playerChunk.z - chunkRadius; cz <= playerChunk.z + chunkRadius; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                for (int localX = 0; localX < 16; localX++) {
                    for (int localZ = 0; localZ < 16; localZ++) {
                        for (int y = mc.world.getBottomY(); y < mc.world.getTopY(); y++) {
                            BlockPos pos = new BlockPos(cx * 16 + localX, y, cz * 16 + localZ);
                            if (mc.world.getBlockState(pos).getBlock() != Blocks.NETHER_PORTAL) continue;
                            Box box = new Box(pos);
                            RenderUtil.drawESPBox(m, box, r, g, b, a, 1.5f);
                            RenderUtil.drawFilledBox(m, box, r, g, b, 0.15f);
                            if (tracers.isEnabled()) {
                                RenderUtil.drawLine3D(m, camVec, Vec3d.ofCenter(pos), r, g, b, a, 1.0f);
                            }
                        }
                    }
                }
            }
        }
    }
}
