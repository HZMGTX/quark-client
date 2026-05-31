package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class ChestESP2 extends Module {

    private final ColorSetting chestColor        = register(new ColorSetting("Chest",         "Normal chest color",        0xFFFF9900));
    private final ColorSetting trappedColor      = register(new ColorSetting("TrappedChest",  "Trapped chest color",       0xFFFF2222));
    private final ColorSetting barrelColor       = register(new ColorSetting("Barrel",        "Barrel color",              0xFF8B5A2B));
    private final ColorSetting shulkerColor      = register(new ColorSetting("Shulker",       "Shulker box color",         0xFFCC44FF));

    public ChestESP2() {
        super("ChestESP2", "ESP for containers with separate colors per type", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;
        MatrixStack m = event.getMatrixStack();
        BlockPos playerPos = mc.player.getBlockPos();
        ChunkPos playerChunk = new ChunkPos(playerPos);
        int chunkRadius = 4;

        for (int cx = playerChunk.x - chunkRadius; cx <= playerChunk.x + chunkRadius; cx++) {
            for (int cz = playerChunk.z - chunkRadius; cz <= playerChunk.z + chunkRadius; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockPos pos = be.getPos();
                    ColorSetting cs;

                    if (be instanceof ChestBlockEntity) {
                        if (mc.world.getBlockState(pos).getBlock() instanceof TrappedChestBlock) {
                            cs = trappedColor;
                        } else {
                            cs = chestColor;
                        }
                    } else if (be instanceof BarrelBlockEntity) {
                        cs = barrelColor;
                    } else if (be instanceof ShulkerBoxBlockEntity) {
                        cs = shulkerColor;
                    } else {
                        continue;
                    }

                    Box box = new Box(pos);
                    RenderUtil.drawESPBox(m, box, cs.getRedF(), cs.getGreenF(), cs.getBlueF(), cs.getAlphaF(), 1.5f);
                    RenderUtil.drawFilledBox(m, box, cs.getRedF(), cs.getGreenF(), cs.getBlueF(), cs.getAlphaF() * 0.1f);
                }
            }
        }
    }
}
