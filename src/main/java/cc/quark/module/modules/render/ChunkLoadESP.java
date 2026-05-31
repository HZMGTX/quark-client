package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class ChunkLoadESP extends Module {

    private final ColorSetting loadedColor = register(new ColorSetting("LoadedColor", "Color for loaded chunks", 0x3300FF44));

    public ChunkLoadESP() {
        super("ChunkLoadESP", "Visualizes loaded vs unloaded chunks around the player", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        float r = loadedColor.getRedF(), g = loadedColor.getGreenF(), b = loadedColor.getBlueF(), a = loadedColor.getAlphaF();
        BlockPos playerPos = mc.player.getBlockPos();
        ChunkPos playerChunk = new ChunkPos(playerPos);
        int radius = 6;

        for (int cx = playerChunk.x - radius; cx <= playerChunk.x + radius; cx++) {
            for (int cz = playerChunk.z - radius; cz <= playerChunk.z + radius; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;
                Box box = new Box(cx * 16, playerPos.getY() - 1, cz * 16, cx * 16 + 16, playerPos.getY(), cz * 16 + 16);
                RenderUtil.drawFilledBox(m, box, r, g, b, a);
                RenderUtil.drawESPBox(m, box, r, g, b, Math.min(a * 2, 1f), 1.0f);
            }
        }
    }
}
