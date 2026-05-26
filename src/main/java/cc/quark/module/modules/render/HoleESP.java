package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.event.events.EventRender3D;
import cc.quark.gui.ClickGUI;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class HoleESP extends Module {

    private final BoolSetting obsidianOnly = register(new BoolSetting(
            "Obsidian Only", "Only highlight holes made of obsidian/bedrock", false));

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Search range in blocks", 16, 4, 64));

    private final BoolSetting showCount = register(new BoolSetting(
            "Show Count", "Show hole count on screen", true));

    private final List<HoleInfo> foundHoles = new ArrayList<>();

    public HoleESP() {
        super("HoleESP", "Highlights safe 1x1 holes in the ground", Category.RENDER);
    }

    private static class HoleInfo {
        final BlockPos pos;
        final int type;
        HoleInfo(BlockPos pos, int type) { this.pos = pos; this.type = type; }
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (mc.world == null || mc.player == null) return;

        foundHoles.clear();

        BlockPos center = mc.player.getBlockPos();
        int r = (int) range.get();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = -r; y <= r; y++) {
                    BlockPos pos = center.add(x, y, z);

                    double distSq = mc.player.getPos().squaredDistanceTo(
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    if (distSq > range.get() * range.get()) continue;

                    if (!mc.world.getBlockState(pos).isAir()) continue;
                    if (!mc.world.getBlockState(pos.up()).isAir()) continue;

                    BlockPos floor = pos.down();
                    BlockPos north = floor.north();
                    BlockPos south = floor.south();
                    BlockPos west  = floor.west();
                    BlockPos east  = floor.east();

                    if (!isSolid(floor) || !isSolid(north) || !isSolid(south)
                            || !isSolid(west) || !isSolid(east)) continue;

                    boolean floorObs = isObsidian(floor);
                    boolean sidesObs = isObsidian(north) && isObsidian(south)
                                    && isObsidian(west) && isObsidian(east);

                    if (obsidianOnly.isEnabled() && !floorObs && !sidesObs) continue;

                    boolean floorBed  = isBedrock(floor);
                    boolean sidesBed  = isBedrock(north) && isBedrock(south)
                                     && isBedrock(west) && isBedrock(east);

                    int holeType;
                    if (floorObs || sidesObs) {
                        holeType = floorBed || sidesBed ? 2 : 1;
                    } else if (floorBed || sidesBed) {
                        holeType = 3;
                    } else {
                        holeType = 0;
                    }

                    foundHoles.add(new HoleInfo(pos, holeType));
                }
            }
        }

        for (HoleInfo hole : foundHoles) {
            float hr, hg, hb;
            switch (hole.type) {
                case 1 -> { hr = 0.0f; hg = 0.4f; hb = 1.0f; }
                case 2 -> {
                    int accent = ClickGUI.getAccentColor();
                    hr = ((accent >> 16) & 0xFF) / 255f;
                    hg = ((accent >> 8) & 0xFF) / 255f;
                    hb = (accent & 0xFF) / 255f;
                }
                case 3 -> { hr = 1.0f; hg = 0.75f; hb = 0.0f; }
                default -> { hr = 0.5f; hg = 0.5f; hb = 0.5f; }
            }

            BlockPos pos = hole.pos;
            Box topSlice = new Box(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 0.05, pos.getZ() + 1);
            RenderUtil.drawFilledBox(event.getMatrixStack(), topSlice, hr, hg, hb, 0.5f);
            Box fullBox = new Box(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            RenderUtil.drawESPBox(event.getMatrixStack(), fullBox, hr, hg, hb, 0.9f, 1.5f);
        }
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showCount.isEnabled() || mc.player == null) return;
        DrawContext ctx = event.getDrawContext();
        RenderUtil.drawCustomText(ctx, "Holes: " + foundHoles.size(), 4, 4, 0xFFFFFFFF);
    }

    private boolean isSolid(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return !state.isAir() && state.isSolidBlock(mc.world, pos);
    }

    private boolean isObsidian(BlockPos pos) {
        Block b = mc.world.getBlockState(pos).getBlock();
        return b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN;
    }

    private boolean isBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock() == Blocks.BEDROCK;
    }
}
