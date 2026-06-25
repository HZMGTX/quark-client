package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

/**
 * StorageESP2 - enhanced storage block ESP with per-type color customisation
 * and both outline and fill rendering.  Extends the feature set of
 * {@link StorageESP} with trapped-chest detection and a distinct color palette.
 */
public class StorageESP2 extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Search range in blocks", 48, 8, 96));
    private final BoolSetting chests = register(new BoolSetting(
            "Chests", "Show regular chests", true));
    private final BoolSetting trappedChests = register(new BoolSetting(
            "Trapped Chests", "Show trapped chests in a different color", true));
    private final BoolSetting barrels = register(new BoolSetting(
            "Barrels", "Show barrels", true));
    private final BoolSetting shulkers = register(new BoolSetting(
            "Shulker Boxes", "Show shulker boxes", true));
    private final BoolSetting enderChests = register(new BoolSetting(
            "Ender Chests", "Show ender chests", true));
    private final BoolSetting furnaces = register(new BoolSetting(
            "Furnaces", "Show furnaces and blast furnaces", false));
    private final BoolSetting hoppers = register(new BoolSetting(
            "Hoppers", "Show hoppers", false));
    private final BoolSetting dispensers = register(new BoolSetting(
            "Dispensers", "Show dispensers and droppers", false));
    private final BoolSetting fill = register(new BoolSetting(
            "Fill", "Fill the ESP box with a translucent color", true));

    public StorageESP2() {
        super("Storage ESP+", "Enhanced ESP for all storage blocks with per-type colors", Category.RENDER);
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

                    RenderUtil.drawESPBox(matrices, box, r, g, b, 1.0f, 1.5f);
                    if (fill.isEnabled()) {
                        RenderUtil.drawFilledBox(matrices, box, r, g, b, 0.15f);
                    }
                }
            }
        }
    }

    private float[] getColor(BlockEntity be) {
        if (be instanceof ChestBlockEntity) {
            // Distinguish trapped chests via block state
            BlockState state = mc.world.getBlockState(be.getPos());
            if (state.getBlock() instanceof TrappedChestBlock) {
                return trappedChests.isEnabled() ? new float[]{1.0f, 0.3f, 0.0f} : null; // red-orange
            }
            return chests.isEnabled() ? new float[]{1.0f, 0.85f, 0.0f} : null; // gold
        }
        if (be instanceof BarrelBlockEntity && barrels.isEnabled())
            return new float[]{0.6f, 0.4f, 0.1f};  // brown
        if (be instanceof ShulkerBoxBlockEntity && shulkers.isEnabled())
            return new float[]{0.75f, 0.0f, 0.75f}; // purple
        if (be instanceof EnderChestBlockEntity && enderChests.isEnabled())
            return new float[]{0.35f, 0.0f, 0.6f};  // dark purple
        if ((be instanceof FurnaceBlockEntity || be instanceof BlastFurnaceBlockEntity
                || be instanceof SmokerBlockEntity) && furnaces.isEnabled())
            return new float[]{0.85f, 0.4f, 0.1f};  // orange
        if (be instanceof HopperBlockEntity && hoppers.isEnabled())
            return new float[]{0.55f, 0.55f, 0.55f}; // gray
        if ((be instanceof DispenserBlockEntity || be instanceof DropperBlockEntity) && dispensers.isEnabled())
            return new float[]{0.2f, 0.7f, 0.9f};   // cyan
        return null;
    }
}
