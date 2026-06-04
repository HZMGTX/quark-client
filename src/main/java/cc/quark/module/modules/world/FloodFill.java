package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * FloodFill - Places blocks on top of nearby water or lava source blocks to fill them.
 */
public class FloodFill extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Search radius in blocks", 4.0, 1.0, 8.0));
    private final BoolSetting lava = register(new BoolSetting(
            "Lava", "Fill lava sources", true));
    private final BoolSetting water = register(new BoolSetting(
            "Water", "Fill water sources", true));

    private final TimerUtil timer = new TimerUtil();

    public FloodFill() {
        super("FloodFill", "Fills water/lava areas with blocks", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;

        // Need a block item in hand
        var held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof BlockItem)) return;

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, 1, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            var state = mc.world.getBlockState(pos);

            boolean isLava  = state.isOf(Blocks.LAVA)  && lava.isEnabled();
            boolean isWater = state.isOf(Blocks.WATER) && water.isEnabled();

            if (!isLava && !isWater) continue;

            // Place block on top of the liquid
            BlockPos above = pos.up();
            if (!mc.world.getBlockState(above).isAir()) continue;

            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            return;
        }
    }
}
