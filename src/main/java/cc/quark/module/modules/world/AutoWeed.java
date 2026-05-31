package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoWeed extends Module {

    private final BoolSetting keepSeeds = register(new BoolSetting("KeepSeeds", "Replant seeds after breaking mature crops", true));

    private final TimerUtil timer = new TimerUtil();
    private int cycleStep = 0;

    public AutoWeed() {
        super("AutoWeed", "Breaks fully-grown crops and replants based on cycle", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
        cycleStep = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(80)) return;

        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-5, -2, -5), center.add(5, 2, 5))) {
            BlockState state = mc.world.getBlockState(pos);
            Block block = state.getBlock();
            if (!(block instanceof CropBlock crop)) continue;
            if (!crop.isMature(state)) continue;

            Item seed = getSeed(block);
            mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);

            if (keepSeeds.isEnabled() && seed != null) {
                int seedSlot = findSeedSlot(seed);
                if (seedSlot != -1) {
                    BlockPos farmland = pos.down();
                    if (mc.world.getBlockState(farmland).isOf(Blocks.FARMLAND)) {
                        int saved = mc.player.getInventory().selectedSlot;
                        mc.player.getInventory().selectedSlot = seedSlot;
                        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(farmland).add(0, 0.5, 0), Direction.UP, farmland.toImmutable(), false);
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                        mc.player.getInventory().selectedSlot = saved;
                    }
                }
            }
            timer.reset();
            return;
        }
    }

    private Item getSeed(Block block) {
        if (block == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS) return Items.CARROT;
        if (block == Blocks.POTATOES) return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        return null;
    }

    private int findSeedSlot(Item seed) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == seed) return i;
        }
        return -1;
    }
}
