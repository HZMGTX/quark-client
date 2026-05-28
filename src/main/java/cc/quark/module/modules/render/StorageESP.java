package cc.quark.module.modules.render;

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

    private final IntSetting range     = register(new IntSetting("Range",        "Search range in blocks",     32, 10, 64));
    private final BoolSetting chests   = register(new BoolSetting("Chests",      "Show chests and barrels",    true));
    private final BoolSetting shulkers = register(new BoolSetting("Shulkers",    "Show shulker boxes",         true));
    private final BoolSetting enderChests = register(new BoolSetting("EnderChests", "Show ender chests",       true));
    private final BoolSetting furnaces = register(new BoolSetting("Furnaces",    "Show furnaces",              false));
    private final BoolSetting hoppers  = register(new BoolSetting("Hoppers",     "Show hoppers",               false));
    private final BoolSetting showFill = register(new BoolSetting("Fill",        "Fill box with color",        true));

    public StorageESP() {
        super("StorageESP", "Shows storage blocks through walls", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;
        MatrixStack matrices = event.getMatrixStack();

        int chunkRadius = (range.get() >> 4) + 1;
        ChunkPos playerChunk = new ChunkPos(mc.player.getBlockPos());

        for (int cx = playerChunk.x - chunkRadius; cx <= playerChunk.x + chunkRadius; cx++) {
            for (int cz = playerChunk.z - chunkRadius; cz <= playerChunk.z + chunkRadius; cz++) {
                WorldChunk chunk = mc.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (mc.player.getPos().distanceTo(be.getPos().toCenterPos()) > range.get()) continue;

                    float[] color = getColor(be);
                    if (color == null) continue;

                    float r = color[0], g = color[1], b = color[2];
                    BlockPos pos = be.getPos();
                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                                      pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                    RenderUtil.drawESPBox(matrices, box, r, g, b, 0.9f, 1.5f);
                    if (showFill.isEnabled()) {
                        RenderUtil.drawFilledBox(matrices, box, r, g, b, 0.2f);
                    }
                }
            }
        }
    }

    private float[] getColor(BlockEntity be) {
        if ((be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity) && chests.isEnabled())
            return new float[]{1.0f, 0.67f, 0.0f};
        if (be instanceof ShulkerBoxBlockEntity && shulkers.isEnabled())
            return new float[]{0.67f, 0.33f, 1.0f};
        if (be instanceof EnderChestBlockEntity && enderChests.isEnabled())
            return new float[]{0.35f, 0.0f, 0.55f};
        if ((be instanceof FurnaceBlockEntity || be instanceof BlastFurnaceBlockEntity
                || be instanceof SmokerBlockEntity) && furnaces.isEnabled())
            return new float[]{1.0f, 0.33f, 0.0f};
        if (be instanceof HopperBlockEntity && hoppers.isEnabled())
            return new float[]{0.55f, 0.55f, 0.55f};
        return null;
    }
}
