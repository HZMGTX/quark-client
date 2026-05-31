package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
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

public class BonemealFarm extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius to search for crops", 5, 1, 10));

    private final BoolSetting onlyIfGrowable = register(new BoolSetting(
            "OnlyIfGrowable", "Only apply bonemeal to crops that can still grow", true));

    private final TimerUtil timer = new TimerUtil();

    public BonemealFarm() {
        super("BonemealFarm", "Auto-uses bonemeal on crops in range", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(150)) return;
        timer.reset();

        int bonemealSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.BONE_MEAL) {
                bonemealSlot = i;
                break;
            }
        }
        if (bonemealSlot == -1) return;

        BlockPos center = mc.player.getBlockPos();
        int r = radius.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            BlockState state = mc.world.getBlockState(pos);
            Block block = state.getBlock();
            if (!(block instanceof CropBlock) && !(block instanceof SaplingBlock)
                    && !(block instanceof SugarCaneBlock) && !(block instanceof BambooBlock)) continue;

            if (onlyIfGrowable.isEnabled() && block instanceof CropBlock crop) {
                if (crop.isMature(state)) continue;
            }

            int saved = mc.player.getInventory().selectedSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(bonemealSlot));
            mc.player.getInventory().selectedSlot = bonemealSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saved));
            mc.player.getInventory().selectedSlot = saved;
            return;
        }
    }
}
