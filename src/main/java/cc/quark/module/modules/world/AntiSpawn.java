package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AntiSpawn — prevents hostile mob spawning by automatically placing torches
 * in dark areas within a configurable radius.
 */
public class AntiSpawn extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Radius around player to scan for dark spots", 8, 2, 16));
    private final IntSetting lightThreshold = register(new IntSetting(
            "Light Threshold", "Place light when block-light level is below this value", 7, 1, 15));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between torch placements", 400, 100, 3000));
    private final BoolSetting torchOnly = register(new BoolSetting(
            "Torch Only", "Only use torches (skip glowstone/lantern)", true));

    private final TimerUtil timer = new TimerUtil();

    public AntiSpawn() {
        super("AntiSpawn", "Prevents hostile mob spawning by lighting up dark areas automatically", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    private int findLightItem() {
        for (int i = 0; i < 9; i++) {
            var item = mc.player.getInventory().getStack(i).getItem();
            if (item == Items.TORCH) return i;
            if (!torchOnly.isEnabled() && (item == Items.GLOWSTONE || item == Items.LANTERN ||
                    item == Items.SOUL_TORCH || item == Items.WALL_TORCH)) return i;
        }
        return -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        int itemSlot = findLightItem();
        if (itemSlot == -1) return;

        int r = range.get();
        int threshold = lightThreshold.get();
        BlockPos origin = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(origin.add(-r, -2, -r), origin.add(r, 2, r))) {
            if (mc.world.getBlockState(pos).isSolidBlock(mc.world, pos)) continue;
            if (!mc.world.getBlockState(pos).isAir()) continue;

            BlockPos below = pos.down();
            if (mc.world.getBlockState(below).isAir()) continue;
            if (!mc.world.getBlockState(below).isSolidBlock(mc.world, below)) continue;

            if (mc.world.getLightLevel(pos) >= threshold) continue;

            double dist = mc.player.getPos().distanceTo(Vec3d.ofCenter(pos));
            if (dist > r) continue;

            int savedSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = itemSlot;

            Vec3d hitVec = Vec3d.ofCenter(below).add(0, 0.5, 0);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, below, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);

            mc.player.getInventory().selectedSlot = savedSlot;
            timer.reset();
            return;
        }
    }
}
