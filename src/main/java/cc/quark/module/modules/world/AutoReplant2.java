package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoReplant2 extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Tick delay between actions (ms)", 200, 50, 1000));
    private final IntSetting range = register(new IntSetting("Range", "Scan radius for farmland", 5, 1, 10));
    private final BoolSetting autoHarvest = register(new BoolSetting("AutoHarvest", "Break fully grown crops before replanting", false));

    private final TimerUtil timer = new TimerUtil();

    public AutoReplant2() {
        super("AutoReplant2", "Auto-replants seeds on farmland with configurable range and delay", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        timer.reset();

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();

        // AutoHarvest: break fully grown crops first
        if (autoHarvest.isEnabled()) {
            for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
                BlockState state = mc.world.getBlockState(pos);
                Block block = state.getBlock();
                if (isMatureCrop(state, block)) {
                    mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    return;
                }
            }
        }

        // Replant: find farmland with air above and place seeds
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.FARMLAND)) continue;
            BlockPos above = pos.up();
            if (!mc.world.getBlockState(above).isAir()) continue;

            Item seed = findAnySeed();
            if (seed == null) continue;

            int seedSlot = findSlotForItem(seed);
            if (seedSlot < 0) continue;

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = seedSlot;

            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);

            mc.player.getInventory().selectedSlot = prevSlot;
            return;
        }
    }

    private boolean isMatureCrop(BlockState state, Block block) {
        if (block instanceof CropBlock crop) return crop.isMature(state);
        if (block == Blocks.NETHER_WART) {
            return state.contains(Properties.AGE_3) && state.get(Properties.AGE_3) == 3;
        }
        return false;
    }

    private Item findAnySeed() {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            Item item = inv.getStack(i).getItem();
            if (isSeed(item)) return item;
        }
        return null;
    }

    private int findSlotForItem(Item item) {
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(item)) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (inv.getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    private boolean isSeed(Item item) {
        return item == Items.WHEAT_SEEDS
                || item == Items.CARROT
                || item == Items.POTATO
                || item == Items.BEETROOT_SEEDS
                || item == Items.MELON_SEEDS
                || item == Items.PUMPKIN_SEEDS
                || item == Items.NETHER_WART;
    }
}
