package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * BlockSpammer - Rapidly places blocks.
 * Line mode: places blocks in a straight line in the facing direction.
 * Plane mode: places a 3x3 plane of blocks on the ground.
 */
public class BlockSpammer extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between placements", 100, 0, 500));
    private final BoolSetting line  = register(new BoolSetting("Line",  "Place in a line",  true));
    private final BoolSetting plane = register(new BoolSetting("Plane", "Place in a 3x3 plane", false));

    private final TimerUtil timer = new TimerUtil();
    private int lineStep = 0;

    public BlockSpammer() {
        super("BlockSpammer", "Rapidly places blocks in a pattern", Category.WORLD);
    }

    @Override
    public void onEnable() {
        lineStep = 0;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        var held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof BlockItem)) return;

        if (plane.isEnabled()) {
            placePlane();
        } else if (line.isEnabled()) {
            placeLine();
        }

        timer.reset();
    }

    private void placeLine() {
        var facing = mc.player.getHorizontalFacing();
        BlockPos base = mc.player.getBlockPos().offset(facing, lineStep + 1).down();
        if (mc.world.getBlockState(base).isAir()) {
            placeAt(base);
            lineStep++;
        } else {
            lineStep = 0;
        }
    }

    private void placePlane() {
        BlockPos center = mc.player.getBlockPos().down();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos pos = center.add(dx, 0, dz);
                if (mc.world.getBlockState(pos).isAir()) {
                    placeAt(pos);
                    return; // One per tick
                }
            }
        }
    }

    private void placeAt(BlockPos pos) {
        BlockPos support = pos.down();
        if (!mc.world.getBlockState(support).isSolidBlock(mc.world, support)) return;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(support), Direction.UP, support, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
