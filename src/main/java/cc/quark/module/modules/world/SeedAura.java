package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * SeedAura - Scans nearby farmland and plants seeds from the hotbar.
 */
public class SeedAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Plant range in blocks", 4.0, 1.0, 6.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between plantings", 200, 50, 2000));

    private final TimerUtil timer = new TimerUtil();

    private static final Item[] SEEDS = {
            Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO,
            Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS
    };

    public SeedAura() {
        super("SeedAura", "Auto-plants seeds on farmland", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        int r = (int) Math.ceil(range.get());
        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;

            // Must be farmland with air above
            if (!mc.world.getBlockState(pos).isOf(Blocks.FARMLAND)) continue;
            BlockPos above = pos.up();
            if (!mc.world.getBlockState(above).isAir()) continue;

            // Find a seed in hotbar
            int seedSlot = findSeedSlot();
            if (seedSlot == -1) return;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = seedSlot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
            return;
        }
    }

    private int findSeedSlot() {
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            for (Item seed : SEEDS) {
                if (item == seed) return i;
            }
        }
        return -1;
    }
}
