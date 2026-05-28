package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ColorSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

public class ChestTracers extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Tracer range in blocks", 50.0, 8.0, 100.0));
    private final ColorSetting chestColor = register(new ColorSetting("Chest Color", "Chest/barrel tracer color", 0xFFFFAA00));
    private final ColorSetting enderColor = register(new ColorSetting("Ender Color", "Ender chest tracer color", 0xFF9933FF));
    private final ColorSetting shulkerColor = register(new ColorSetting("Shulker Color", "Shulker box tracer color", 0xFFCC66FF));

    public ChestTracers() {
        super("ChestTracers", "Draws tracer lines to nearby storage containers", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        MatrixStack matrices = event.getMatrixStack();
        float td = event.getTickDelta();
        Vec3d from = mc.player.getCameraPosVec(td);
        double maxRange = range.get();

        BlockPos playerPos = mc.player.getBlockPos();
        int chunkRadius = ((int) maxRange >> 4) + 1;
        ChunkPos playerChunk = new ChunkPos(playerPos);

        for (int cx = playerChunk.x - chunkRadius; cx <= playerChunk.x + chunkRadius; cx++) {
            for (int cz = playerChunk.z - chunkRadius; cz <= playerChunk.z + chunkRadius; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    BlockPos pos = be.getPos();
                    if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > maxRange) continue;

                    float r, g, b, a;

                    if (be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) {
                        r = chestColor.getRedF();
                        g = chestColor.getGreenF();
                        b = chestColor.getBlueF();
                        a = chestColor.getAlphaF();
                    } else if (be instanceof EnderChestBlockEntity) {
                        r = enderColor.getRedF();
                        g = enderColor.getGreenF();
                        b = enderColor.getBlueF();
                        a = enderColor.getAlphaF();
                    } else if (be instanceof ShulkerBoxBlockEntity) {
                        r = shulkerColor.getRedF();
                        g = shulkerColor.getGreenF();
                        b = shulkerColor.getBlueF();
                        a = shulkerColor.getAlphaF();
                    } else {
                        continue;
                    }

                    RenderUtil.drawLine3D(matrices, from, Vec3d.ofCenter(pos), r, g, b, a, 1.0f);
                }
            }
        }
    }
}
