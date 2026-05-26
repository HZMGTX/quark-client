package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * HoleESP - finds 1x1x2 holes surrounded by bedrock or obsidian, useful for crystal PvP.
 *
 * Color scheme:
 *   Full bedrock hole  â†’ green  (0.0, 1.0, 0.2)
 *   Mixed obsidian     â†’ orange (1.0, 0.5, 0.0)
 *   Full obsidian hole â†’ red    (1.0, 0.1, 0.1)
 */
public class HoleESP extends Module {

    private static final int SCAN_RADIUS = 10;

    public HoleESP() {
        super("HoleESP", "Highlights safe bedrock/obsidian holes for crystal PvP", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        BlockPos center = mc.player.getBlockPos();

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -4; y <= 2; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    BlockPos floorPos = center.add(x, y, z);

                    // The hole occupies floorPos+1 and floorPos+2 (must be air)
                    BlockPos hole1 = floorPos.up(1);
                    BlockPos hole2 = floorPos.up(2);

                    if (!mc.world.getBlockState(hole1).isAir()) continue;
                    if (!mc.world.getBlockState(hole2).isAir()) continue;

                    // Floor must be blast-resistant
                    if (!isSafe(floorPos)) continue;

                    // Check four horizontal neighbours at floor level
                    BlockPos north = floorPos.north();
                    BlockPos south = floorPos.south();
                    BlockPos west  = floorPos.west();
                    BlockPos east  = floorPos.east();

                    if (!isSafe(north) || !isSafe(south) || !isSafe(west) || !isSafe(east)) continue;

                    // Determine color based on block types
                    boolean allBedrock = isBedrock(floorPos)
                            && isBedrock(north) && isBedrock(south)
                            && isBedrock(west)  && isBedrock(east);

                    boolean allObsidian = isObsidian(floorPos)
                            && isObsidian(north) && isObsidian(south)
                            && isObsidian(west)  && isObsidian(east);

                    float r, g, b;
                    if (allBedrock) {
                        r = 0.0f; g = 1.0f; b = 0.2f; // green
                    } else if (allObsidian) {
                        r = 1.0f; g = 0.1f; b = 0.1f; // red
                    } else {
                        r = 1.0f; g = 0.5f; b = 0.0f; // orange (mixed)
                    }

                    // Render a box at the floor position (the "inside" of the hole at player feet)
                    Box box = new Box(hole1).union(new Box(hole2));
                    // Shrink slightly for a cleaner look
                    Box renderBox = new Box(
                            box.minX + 0.1, box.minY, box.minZ + 0.1,
                            box.maxX - 0.1, box.maxY, box.maxZ - 0.1
                    );
                    RenderUtil.drawFilledBox(event.getMatrixStack(), renderBox, r, g, b, 0.35f);
                    RenderUtil.drawESPBox(event.getMatrixStack(), renderBox, r, g, b, 0.9f, 1.5f);
                }
            }
        }
    }

    private boolean isSafe(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.OBSIDIAN
                || state.getBlock() == Blocks.CRYING_OBSIDIAN;
    }

    private boolean isBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }

    private boolean isObsidian(BlockPos pos) {
        Block b = mc.world.getBlockState(pos).getBlock();
        return b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN;
    }
}
