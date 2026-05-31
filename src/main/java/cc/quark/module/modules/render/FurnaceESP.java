package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class FurnaceESP extends Module {

    private final ColorSetting activeColor = register(new ColorSetting("ActiveColor", "Color for lit/active furnaces", 0xFFFF6600));

    public FurnaceESP() {
        super("FurnaceESP", "Highlights furnaces that are currently active (lit)", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float r = activeColor.getRedF(), g = activeColor.getGreenF(), b = activeColor.getBlueF(), a = activeColor.getAlphaF();
        BlockPos playerPos = mc.player.getBlockPos();
        int chunkRadius = 4;
        ChunkPos playerChunk = new ChunkPos(playerPos);

        for (int cx = playerChunk.x - chunkRadius; cx <= playerChunk.x + chunkRadius; cx++) {
            for (int cz = playerChunk.z - chunkRadius; cz <= playerChunk.z + chunkRadius; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof AbstractFurnaceBlockEntity furnace)) continue;
                    boolean lit = mc.world.getBlockState(be.getPos()).get(AbstractFurnaceBlock.LIT);
                    if (!lit) continue;
                    BlockPos pos = be.getPos();
                    Box box = new Box(pos);
                    RenderUtil.drawESPBox(m, box, r, g, b, a, 1.5f);
                    RenderUtil.drawFilledBox(m, box, r, g, b, 0.15f);
                }
            }
        }
    }
}
