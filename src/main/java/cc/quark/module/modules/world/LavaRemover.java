package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class LavaRemover extends Module {

    private final IntSetting radius = register(new IntSetting("Radius", "Radius to search for lava sources", 4, 1, 8));

    private final TimerUtil timer = new TimerUtil();

    public LavaRemover() {
        super("LavaRemover", "Picks up lava sources in range using bucket", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(300)) return;

        int bucketSlot = findItem(Items.BUCKET);
        if (bucketSlot == -1) return;

        BlockPos center = mc.player.getBlockPos();
        int r = radius.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            var fluidState = mc.world.getFluidState(pos);
            if (!fluidState.isOf(Fluids.LAVA) || !fluidState.isStill()) continue;

            int saved = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = bucketSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = saved;
            timer.reset();
            return;
        }
    }

    private int findItem(net.minecraft.item.Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}
