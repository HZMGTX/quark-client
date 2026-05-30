package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoFarm2 extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Scan radius for crops", 5, 1, 10));
    private final BoolSetting bamboo = register(new BoolSetting(
            "Bamboo", "Harvest bamboo", true));
    private final BoolSetting cactus = register(new BoolSetting(
            "Cactus", "Harvest cactus", true));
    private final BoolSetting chorus = register(new BoolSetting(
            "Chorus", "Harvest chorus fruit", true));
    private final BoolSetting sugarCane = register(new BoolSetting(
            "SugarCane", "Harvest sugar cane", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoFarm2() {
        super("AutoFarm2", "Improved farming: handles bamboo, cactus, chorus fruit, and sugar cane", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;
        timer.reset();

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 4, r))) {
            Block block = mc.world.getBlockState(pos).getBlock();
            if (shouldHarvest(block, pos)) {
                mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }
    }

    private boolean shouldHarvest(Block block, BlockPos pos) {
        if (bamboo.isEnabled() && block == Blocks.BAMBOO) {
            // Harvest bamboo that's at least 2 high
            BlockPos below = pos.down();
            Block belowBlock = mc.world.getBlockState(below).getBlock();
            return belowBlock == Blocks.BAMBOO || belowBlock == Blocks.BAMBOO_SAPLING;
        }
        if (cactus.isEnabled() && block == Blocks.CACTUS) {
            // Only harvest cactus that has cactus below it
            return mc.world.getBlockState(pos.down()).getBlock() == Blocks.CACTUS;
        }
        if (chorus.isEnabled() && block == Blocks.CHORUS_PLANT) {
            // Harvest top of chorus plant
            return mc.world.getBlockState(pos.up()).isAir();
        }
        if (sugarCane.isEnabled() && block == Blocks.SUGAR_CANE) {
            // Only harvest sugar cane that has sugar cane below it
            return mc.world.getBlockState(pos.down()).getBlock() == Blocks.SUGAR_CANE;
        }
        return false;
    }
}
