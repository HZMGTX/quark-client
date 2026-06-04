package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoCauldron extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect cauldrons", 3.0, 1.0, 6.0));

    private final TimerUtil timer = new TimerUtil();

    public AutoCauldron() {
        super("AutoCauldron", "Auto-fills cauldrons with water/lava buckets", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(400)) return;

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            var state = mc.world.getBlockState(pos);

            boolean isEmptyWaterCauldron = state.isOf(Blocks.CAULDRON);
            boolean isPartialLava = state.isOf(Blocks.LAVA_CAULDRON)
                    && state.contains(LeveledCauldronBlock.LEVEL)
                    && state.get(LeveledCauldronBlock.LEVEL) < 3;
            boolean isPartialWater = state.isOf(Blocks.WATER_CAULDRON)
                    && state.contains(LeveledCauldronBlock.LEVEL)
                    && state.get(LeveledCauldronBlock.LEVEL) < 3;

            if (!isEmptyWaterCauldron && !isPartialLava && !isPartialWater) continue;

            // Choose bucket type based on cauldron
            int bucketSlot = -1;
            if (isPartialLava) {
                bucketSlot = findBucketSlot(true);
            } else {
                bucketSlot = findBucketSlot(false);
                if (bucketSlot == -1) bucketSlot = findBucketSlot(true);
            }
            if (bucketSlot == -1) return;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = bucketSlot;
            Vec3d hitVec = Vec3d.ofCenter(pos).add(0, 0.5, 0);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            return;
        }
        timer.reset();
    }

    private int findBucketSlot(boolean lava) {
        if (mc.player == null) return -1;
        var target = lava ? Items.LAVA_BUCKET : Items.WATER_BUCKET;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(target)) return i < 9 ? i : i;
        }
        return -1;
    }
}
