package cc.quark.module.modules.render;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.entity.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class StorageESP extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Search range", 32, 10, 64));
    private final BoolSetting showCount = register(new BoolSetting("Show Count", "Show item count", true));
    private final BoolSetting chests = register(new BoolSetting("Chests", "Show chests", true));
    private final BoolSetting shulkers = register(new BoolSetting("Shulkers", "Show shulker boxes", true));
    private final BoolSetting furnaces = register(new BoolSetting("Furnaces", "Show furnaces", false));

    public StorageESP() {
        super("StorageESP", "Shows storage blocks with item info", Category.RENDER, 0);
    }

    @Override
    public void onEnable() {
        Quark.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        Quark.getInstance().getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
        MatrixStack matrices = event.getMatrixStack();

        int chunkRadius = (range.getValue() >> 4) + 1;
        ChunkPos playerChunk = new ChunkPos(mc.player.getBlockPos());

        for (int cx = playerChunk.x - chunkRadius; cx <= playerChunk.x + chunkRadius; cx++) {
            for (int cz = playerChunk.z - chunkRadius; cz <= playerChunk.z + chunkRadius; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (mc.player.getPos().distanceTo(be.getPos().toCenterPos()) > range.getValue()) continue;
                    int color = getColor(be);
                    if (color == 0) continue;
                    BlockPos pos = be.getPos();
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                                      pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                    float r = ((color >> 16) & 0xFF) / 255.0f;
                    float g = ((color >> 8) & 0xFF) / 255.0f;
                    float b = (color & 0xFF) / 255.0f;
                    RenderUtil.drawESPBox(matrices, box, r, g, b, 0.85f, 1.5f);
                }
            }
        }
    }

    private int getColor(BlockEntity be) {
        if (be instanceof ChestBlockEntity && chests.getValue()) return 0xFFAA00;
        if (be instanceof BarrelBlockEntity && chests.getValue()) return 0xAA7700;
        if (be instanceof ShulkerBoxBlockEntity && shulkers.getValue()) return 0xAA55FF;
        if (be instanceof FurnaceBlockEntity && furnaces.getValue()) return 0xFF5500;
        if (be instanceof BlastFurnaceBlockEntity && furnaces.getValue()) return 0xFF7700;
        if (be instanceof HopperBlockEntity) return 0x888888;
        return 0;
    }
}
