package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoSeed — automatically plants seeds from inventory onto tilled farmland.
 */
public class AutoSeed extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Radius of farmland to scan", 5, 1, 10));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between seed placements", 250, 50, 2000));
    private final BoolSetting wheat = register(new BoolSetting(
            "Wheat", "Plant wheat seeds", true));
    private final BoolSetting carrots = register(new BoolSetting(
            "Carrots", "Plant carrots", true));
    private final BoolSetting potatoes = register(new BoolSetting(
            "Potatoes", "Plant potatoes", true));
    private final BoolSetting beetroot = register(new BoolSetting(
            "Beetroot", "Plant beetroot seeds", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoSeed() {
        super("AutoSeed", "Automatically plants seeds from inventory on tilled soil", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    private int findSeedSlot() {
        for (int i = 0; i < 9; i++) {
            var item = mc.player.getInventory().getStack(i).getItem();
            if (wheat.isEnabled()   && item == Items.WHEAT_SEEDS)    return i;
            if (carrots.isEnabled() && item == Items.CARROT)          return i;
            if (potatoes.isEnabled()&& item == Items.POTATO)          return i;
            if (beetroot.isEnabled()&& item == Items.BEETROOT_SEEDS)  return i;
        }
        return -1;
    }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        int slot = findSeedSlot();
        if (slot < 0) return;

        int r = range.get();
        BlockPos origin = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(origin.add(-r, -2, -r), origin.add(r, 2, r))) {
            var block = mc.world.getBlockState(pos).getBlock();
            if (block != Blocks.FARMLAND) continue;

            BlockPos above = pos.up();
            if (!mc.world.getBlockState(above).isAir()) continue;

            double dist = mc.player.getPos().distanceTo(Vec3d.ofCenter(pos));
            if (dist > r + 1) continue;

            int saved = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;

            Vec3d hitVec = Vec3d.ofCenter(pos).add(0, 0.5, 0);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);

            mc.player.getInventory().selectedSlot = saved;
            timer.reset();
            return;
        }
    }
}
