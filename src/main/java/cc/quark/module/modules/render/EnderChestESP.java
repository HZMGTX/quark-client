package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class EnderChestESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Ender chest ESP color", 0xFF330066));

    public EnderChestESP() {
        super("EnderChestESP", "ESP for ender chests, distinct from normal chests", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float r = color.getRedF(), g = color.getGreenF(), b = color.getBlueF(), a = color.getAlphaF();
        BlockPos playerPos = mc.player.getBlockPos();
        int chunkRadius = 4;
        ChunkPos playerChunk = new ChunkPos(playerPos);

        for (int cx = playerChunk.x - chunkRadius; cx <= playerChunk.x + chunkRadius; cx++) {
            for (int cz = playerChunk.z - chunkRadius; cz <= playerChunk.z + chunkRadius; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof EnderChestBlockEntity)) continue;
                    BlockPos pos = be.getPos();
                    Box box = new Box(pos);
                    RenderUtil.drawESPBox(m, box, r, g, b, a, 1.5f);
                    RenderUtil.drawFilledBox(m, box, r, g, b, 0.2f);
                }
            }
        }
    }
}
