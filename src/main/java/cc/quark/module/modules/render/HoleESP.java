package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender3D;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class HoleESP extends Module {

    private final BoolSetting obsidianOnly = register(new BoolSetting(
            "Obsidian Only", "Only highlight obsidian holes", false));

    private final IntSetting range = register(new IntSetting(
            "Range", "Search range in blocks", 5, 2, 10));

    public HoleESP() {
        super("HoleESP", "Highlights safe 1x1 holes in the ground", Category.RENDER);
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = -r; y <= r; y++) {
                    BlockPos pos = center.add(x, y, z);

                    if (!mc.world.getBlockState(pos).isAir()) continue;
                    if (!mc.world.getBlockState(pos.up()).isAir()) continue;

                    BlockPos floor = pos.down();
                    BlockPos north = floor.north();
                    BlockPos south = floor.south();
                    BlockPos west  = floor.west();
                    BlockPos east  = floor.east();

                    if (!isSolid(floor) || !isSolid(north) || !isSolid(south)
                            || !isSolid(west) || !isSolid(east)) continue;

                    if (obsidianOnly.isEnabled()) {
                        if (!isObsidian(floor) || !isObsidian(north) || !isObsidian(south)
                                || !isObsidian(west) || !isObsidian(east)) continue;
                    }

                    Box box = new Box(pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                    RenderUtil.drawFilledBox(event.getMatrixStack(), box, 0.0f, 0.5f, 1.0f, 0.3f);
                    RenderUtil.drawESPBox(event.getMatrixStack(), box, 0.0f, 0.5f, 1.0f, 0.9f, 1.5f);
                }
            }
        }
    }

    private boolean isSolid(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return !state.isAir() && state.isSolidBlock(mc.world, pos);
    }

    private boolean isObsidian(BlockPos pos) {
        net.minecraft.block.Block b = mc.world.getBlockState(pos).getBlock();
        return b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN || b == Blocks.BEDROCK;
    }
}
