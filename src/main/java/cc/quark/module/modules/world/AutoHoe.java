package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.HoeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoHoe extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius to search for tillable blocks", 4, 1, 8));
    private final BoolSetting requireHoe = register(new BoolSetting(
            "Require Hoe", "Only till when holding a hoe", true));
    private final TimerUtil timer = new TimerUtil();

    public AutoHoe() {
        super("AutoHoe", "Automatically tills soil with a hoe", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;

        if (requireHoe.isEnabled() && !(mc.player.getMainHandStack().getItem() instanceof HoeItem)) return;

        int r = radius.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, 1, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > r * r) continue;
            if (!mc.world.getBlockState(pos.up()).isAir()) continue;

            boolean tillable = mc.world.getBlockState(pos).isOf(Blocks.DIRT)
                    || mc.world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)
                    || mc.world.getBlockState(pos).isOf(Blocks.DIRT_PATH);
            if (!tillable) continue;

            // Switch to hoe if not already holding one
            int hoeSlot = -1;
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() instanceof HoeItem) {
                    hoeSlot = i;
                    break;
                }
            }
            int savedSlot = mc.player.getInventory().selectedSlot;
            if (hoeSlot != -1) mc.player.getInventory().selectedSlot = hoeSlot;

            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);

            mc.player.getInventory().selectedSlot = savedSlot;
            timer.reset();
            return;
        }
    }
}
