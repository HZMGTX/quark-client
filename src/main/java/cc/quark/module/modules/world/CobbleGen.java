package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CobbleGen extends Module {

    private final IntSetting mineDelay = register(new IntSetting(
            "Mine Delay", "Delay between mining cobblestone (ms)", 250, 50, 1000));
    private final BoolSetting placeLava = register(new BoolSetting(
            "Place Lava", "Automatically place lava bucket for gen setup", false));
    private final BoolSetting placeWater = register(new BoolSetting(
            "Place Water", "Automatically place water bucket for gen setup", false));
    private final TimerUtil timer = new TimerUtil();

    public CobbleGen() {
        super("CobbleGen", "Automatically mines cobblestone from a cobblestone generator", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
        ChatUtil.info("[CobbleGen] Stand next to your cobblestone generator to start mining.");
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(mineDelay.get())) return;

        BlockPos center = mc.player.getBlockPos();

        // Search nearby for cobblestone to mine
        for (BlockPos pos : BlockPos.iterate(center.add(-2, -1, -2), center.add(2, 1, 2))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.COBBLESTONE)) continue;

            mc.interactionManager.attackBlock(pos, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            return;
        }

        // Optionally assist with placing lava/water for gen setup
        if (placeLava.isEnabled()) {
            tryPlaceBucket(Items.LAVA_BUCKET, center);
        }
        if (placeWater.isEnabled()) {
            tryPlaceBucket(Items.WATER_BUCKET, center);
        }

        timer.reset();
    }

    private void tryPlaceBucket(net.minecraft.item.Item bucketItem, BlockPos center) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(bucketItem)) {
                slot = i;
                break;
            }
        }
        if (slot == -1) return;

        // Place at an adjacent air block
        for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            BlockPos target = center.offset(dir);
            if (!mc.world.getBlockState(target).isAir()) continue;
            int saved = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(target), dir.getOpposite(), target.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = saved;
            return;
        }
    }
}
