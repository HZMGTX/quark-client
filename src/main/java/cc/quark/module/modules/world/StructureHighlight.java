package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class StructureHighlight extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Scan radius for structures", 32, 8, 64));
    private final BoolSetting onlyUnexplored = register(new BoolSetting("OnlyUnexplored", "Only highlight structures not near player", true));

    public StructureHighlight() {
        super("StructureHighlight", "Highlights dungeon and temple structures via block pattern detection", Category.WORLD);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.player == null || mc.world == null) return;

        MatrixStack matrices = event.getMatrixStack();
        int r = range.get();
        BlockPos center = mc.player.getBlockPos();

        List<BlockPos> structureCenters = findStructures(center, r);

        for (BlockPos sp : structureCenters) {
            if (onlyUnexplored.isEnabled()) {
                double dist = mc.player.squaredDistanceTo(sp.getX(), sp.getY(), sp.getZ());
                if (dist < 100) continue; // Skip if within 10 blocks
            }

            Box box = new Box(
                    sp.getX() - 8, sp.getY() - 2, sp.getZ() - 8,
                    sp.getX() + 8, sp.getY() + 8, sp.getZ() + 8
            );
            RenderUtil.drawESPBox(matrices, box, 1.0f, 0.5f, 0.0f, 0.8f, 2.0f);
            RenderUtil.drawFilledBox(matrices, box, 1.0f, 0.5f, 0.0f, 0.05f);
        }
    }

    private List<BlockPos> findStructures(BlockPos center, int r) {
        List<BlockPos> found = new ArrayList<>();
        int worldBottom = mc.world.getBottomY();
        int worldTop = mc.world.getTopY();

        for (int x = center.getX() - r; x <= center.getX() + r; x += 4) {
            for (int z = center.getZ() - r; z <= center.getZ() + r; z += 4) {
                for (int y = worldBottom; y < worldTop; y += 4) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (isDungeonCenter(pos) || isTempleCenter(pos)) {
                        // Avoid duplicates within 16 blocks
                        boolean dupe = false;
                        for (BlockPos existing : found) {
                            if (existing.getManhattanDistance(pos) < 16) {
                                dupe = true;
                                break;
                            }
                        }
                        if (!dupe) found.add(pos.toImmutable());
                    }
                }
            }
        }
        return found;
    }

    private boolean isDungeonCenter(BlockPos pos) {
        // Dungeons: mossy cobblestone + cobblestone room
        int mossy = 0, cobble = 0;
        for (BlockPos p : BlockPos.iterate(pos.add(-4, -1, -4), pos.add(4, 2, 4))) {
            Block b = mc.world.getBlockState(p).getBlock();
            if (b == Blocks.MOSSY_COBBLESTONE) mossy++;
            if (b == Blocks.COBBLESTONE) cobble++;
        }
        return mossy >= 6 && cobble >= 10;
    }

    private boolean isTempleCenter(BlockPos pos) {
        // Desert temples: sandstone + chiseled sandstone pattern
        int sandstone = 0, chiseled = 0;
        for (BlockPos p : BlockPos.iterate(pos.add(-8, -2, -8), pos.add(8, 8, 8))) {
            Block b = mc.world.getBlockState(p).getBlock();
            if (b == Blocks.SANDSTONE) sandstone++;
            if (b == Blocks.CHISELED_SANDSTONE) chiseled++;
        }
        return sandstone >= 20 && chiseled >= 3;
    }
}
