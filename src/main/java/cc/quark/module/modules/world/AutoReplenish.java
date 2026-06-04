package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoReplenish extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to detect depleted farming spots", 4.0, 1.0, 8.0));

    private final TimerUtil timer = new TimerUtil();

    public AutoReplenish() {
        super("AutoReplenish", "Automatically replenishes farming materials (seeds, bonemeal, saplings)", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(300)) return;

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        // Scan for empty farmland and replant seeds
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;

            Block ground = mc.world.getBlockState(pos).getBlock();
            if (ground != Blocks.FARMLAND) continue;

            BlockPos above = pos.up();
            if (!mc.world.getBlockState(above).isAir()) continue;

            // Try to place a seed on the empty farmland
            Item seed = pickSeed();
            if (seed == null) break;
            int slot = findHotbarSlot(seed);
            if (slot == -1) {
                // Try to move from main inventory to hotbar
                slot = findInventorySlot(seed);
                if (slot != -1) {
                    mc.interactionManager.clickSlot(
                            mc.player.playerScreenHandler.syncId, slot, 0,
                            SlotActionType.QUICK_MOVE, mc.player);
                    timer.reset();
                    return;
                }
                continue;
            }

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            Vec3d hitVec = Vec3d.ofCenter(pos).add(0, 0.5, 0);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            return;
        }
        timer.reset();
    }

    private Item pickSeed() {
        Item[] seeds = {Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO,
                Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS};
        for (Item s : seeds) {
            if (findHotbarSlot(s) != -1 || findInventorySlot(s) != -1) return s;
        }
        return null;
    }

    private int findHotbarSlot(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    private int findInventorySlot(Item item) {
        if (mc.player == null) return -1;
        for (int i = 9; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }
}
