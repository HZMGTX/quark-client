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

import java.util.Random;

public class SlimeChunkESP extends Module {

    private final ColorSetting color = register(new ColorSetting("Color", "Slime chunk overlay color", 0x6633FF33));

    public SlimeChunkESP() {
        super("SlimeChunkESP", "Highlights slime chunks with a colored overlay", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        BlockPos playerPos = mc.player.getBlockPos();
        ChunkPos playerChunk = new ChunkPos(playerPos);
        long worldSeed = mc.world.getSeed();
        int radius = 5;

        for (int cx = playerChunk.x - radius; cx <= playerChunk.x + radius; cx++) {
            for (int cz = playerChunk.z - radius; cz <= playerChunk.z + radius; cz++) {
                if (!isSlimeChunk(worldSeed, cx, cz)) continue;
                Box box = new Box(cx * 16, playerPos.getY() - 1, cz * 16, cx * 16 + 16, playerPos.getY(), cz * 16 + 16);
                RenderUtil.drawFilledBox(m, box, color.getRedF(), color.getGreenF(), color.getBlueF(), color.getAlphaF());
                RenderUtil.drawESPBox(m, box, color.getRedF(), color.getGreenF(), color.getBlueF(), 1.0f, 1.0f);
            }
        }
    }

    private boolean isSlimeChunk(long seed, int chunkX, int chunkZ) {
        Random rng = new Random(seed +
                (long) (chunkX * chunkX * 0x4c1906) +
                (long) (chunkX * 0x5ac0db) +
                (long) (chunkZ * chunkZ) * 0x4307a7L +
                (long) (chunkZ * 0x5f24f) ^ 0x3ad8025f);
        return rng.nextInt(10) == 0;
    }
}
