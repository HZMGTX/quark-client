package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoBee - Harvests honey bottles or honeycomb from full beehives.
 * Uses a glass bottle or shears from the hotbar.
 */
public class AutoBee extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Search range for beehives", 3.0, 1.0, 6.0));

    private final TimerUtil timer = new TimerUtil();

    public AutoBee() {
        super("AutoBee", "Manages beehive collecting and honey", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(800)) return;

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;

            var state = mc.world.getBlockState(pos);
            if (!state.isOf(Blocks.BEEHIVE) && !state.isOf(Blocks.BEE_NEST)) continue;

            // Check honey level - level 5 means full
            int honeyLevel = state.get(BeehiveBlock.HONEY_LEVEL);
            if (honeyLevel < 5) continue;

            BlockEntity be = mc.world.getBlockEntity(pos);
            if (!(be instanceof BeehiveBlockEntity)) continue;

            // Try glass bottle first, then shears
            int bottleSlot = findInHotbar(Items.GLASS_BOTTLE);
            int shearsSlot = findInHotbar(Items.SHEARS);

            int useSlot = bottleSlot != -1 ? bottleSlot : shearsSlot;
            if (useSlot == -1) {
                ChatUtil.warn("AutoBee: no glass bottle or shears in hotbar");
                return;
            }

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = useSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.NORTH, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            return;
        }
    }

    private int findInHotbar(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}
