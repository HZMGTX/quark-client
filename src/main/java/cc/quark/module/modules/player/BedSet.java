package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BedBlock;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BedSet extends Module {

    private final BoolSetting autoRun = register(new BoolSetting(
            "AutoRun", "Automatically sleep and wake to update spawn each night", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean didSleep = false;

    public BedSet() {
        super("BedSet", "Auto-sleeps and immediately wakes to set spawn point at night", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        didSleep = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoRun.isEnabled()) return;
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(1500)) return;
        timer.reset();

        World world = mc.world;

        if (mc.player.isSleeping()) {
            mc.player.wakeUp();
            didSleep = false;
            return;
        }

        if (!world.isNight() || didSleep) return;

        BlockPos nearest = findNearestBed();
        if (nearest == null) return;

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(nearest), Direction.UP, nearest, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        didSleep = true;
    }

    private BlockPos findNearestBed() {
        if (mc.player == null || mc.world == null) return null;
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.iterate(
                (int) mc.player.getX() - 8, (int) mc.player.getY() - 2, (int) mc.player.getZ() - 8,
                (int) mc.player.getX() + 8, (int) mc.player.getY() + 2, (int) mc.player.getZ() + 8)) {
            if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
                double d = mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos.toImmutable();
                }
            }
        }
        return best;
    }
}
