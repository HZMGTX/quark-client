package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoHarvest2 extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Block radius to scan for fully grown crops", 4, 1, 8));

    private final TimerUtil timer = new TimerUtil();

    public AutoHarvest2() {
        super("AutoHarvest2", "Harvests fully grown crops in range then replants automatically", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(300)) return;
        timer.reset();

        int r = radius.get();
        BlockPos origin = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(
                origin.getX() - r, origin.getY() - 2, origin.getZ() - r,
                origin.getX() + r, origin.getY() + 2, origin.getZ() + r)) {

            var state = mc.world.getBlockState(pos);
            Block block = state.getBlock();

            if (!isFullyGrown(block, state)) continue;

            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
            mc.interactionManager.attackBlock(pos, Direction.UP);

            replant(pos, block);
            return;
        }
    }

    private boolean isFullyGrown(Block block, net.minecraft.block.BlockState state) {
        if (block instanceof CropBlock crop) {
            return crop.isMature(state);
        }
        if (block instanceof NetherWartBlock) {
            return state.get(NetherWartBlock.AGE) == 3;
        }
        if (block instanceof CocoaBlock) {
            return state.get(CocoaBlock.AGE) == 2;
        }
        return false;
    }

    private void replant(BlockPos pos, Block block) {
        if (mc.player == null || mc.interactionManager == null) return;

        net.minecraft.item.Item seed = getSeed(block);
        if (seed == null) return;

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == seed) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                mc.player.getInventory().selectedSlot = i;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.down(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
                mc.player.getInventory().selectedSlot = prev;
                return;
            }
        }
    }

    private net.minecraft.item.Item getSeed(Block block) {
        if (block instanceof WheatBlock) return Items.WHEAT_SEEDS;
        if (block instanceof CarrotsBlock) return Items.CARROT;
        if (block instanceof PotatoesBlock) return Items.POTATO;
        if (block instanceof BeetrootsBlock) return Items.BEETROOT_SEEDS;
        if (block instanceof NetherWartBlock) return Items.NETHER_WART;
        return null;
    }
}
