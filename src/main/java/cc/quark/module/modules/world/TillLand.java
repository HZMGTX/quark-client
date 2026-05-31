package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.HoeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TillLand extends Module {

    private final IntSetting radius = register(new IntSetting("Radius", "Radius to search for tillable blocks", 4, 1, 8));

    private final TimerUtil timer = new TimerUtil();

    public TillLand() {
        super("TillLand", "Automatically tills nearby grass/dirt blocks using a hoe", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;

        int hoeSlot = findHoeSlot();
        if (hoeSlot == -1) return;

        BlockPos center = mc.player.getBlockPos();
        int r = radius.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 0, r))) {
            var block = mc.world.getBlockState(pos).getBlock();
            if (block != Blocks.GRASS_BLOCK && block != Blocks.DIRT && block != Blocks.COARSE_DIRT) continue;
            if (!mc.world.getBlockState(pos.up()).isAir()) continue;

            int saved = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = hoeSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = saved;
            timer.reset();
            return;
        }
    }

    private int findHoeSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof HoeItem) return i;
        }
        return -1;
    }
}
