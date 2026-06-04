package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoLight extends Module {

    private final IntSetting    lightLevel = register(new IntSetting   ("Light Level", "Place torch if below this level", 7, 1, 15));
    private final DoubleSetting range      = register(new DoubleSetting("Range",       "Range to check for dark spots",   4.0, 1.0, 8.0));
    private final IntSetting    delay      = register(new IntSetting   ("Delay",       "Delay between placements (ms)",   500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoLight() {
        super("AutoLight", "Automatically places torches in dark areas", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        int torchSlot = findTorchSlot();
        if (torchSlot == -1) return;

        BlockPos player = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());

        for (BlockPos pos : BlockPos.iterate(player.add(-r, -1, -r), player.add(r, 1, r))) {
            if (!mc.world.getBlockState(pos).isAir()) continue;
            if (mc.world.getLightLevel(pos) >= lightLevel.get()) continue;
            if (mc.player.getPos().distanceTo(Vec3d.ofCenter(pos)) > range.get()) continue;

            BlockPos below = pos.down();
            if (mc.world.getBlockState(below).isAir()) continue;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = torchSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(below).add(0, 0.5, 0), Direction.UP, below, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            return;
        }
    }

    private int findTorchSlot() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.TORCH) || stack.isOf(Items.SOUL_TORCH)) return i;
        }
        return -1;
    }
}
