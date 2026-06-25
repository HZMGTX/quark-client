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
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * NetherWart - automates nether wart farming by harvesting fully grown crops
 * and optionally replanting them on soul sand.
 *
 * Harvests nether wart at age 3 by attacking the block. If the Replant setting
 * is enabled and the player has nether wart in their hotbar, it will replant
 * on exposed soul sand patches within range.
 */
public class NetherWart extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Scan radius for nether wart patches", 5, 1, 10));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between actions (lower = faster)", 2, 1, 20));
    private final BoolSetting replant = register(new BoolSetting(
            "Replant", "Replant nether wart on bare soul sand after harvesting", true));
    private final BoolSetting harvestFirst = register(new BoolSetting(
            "Harvest First", "Prioritise harvesting before replanting", true));

    private int ticker = 0;
    private final TimerUtil replantTimer = new TimerUtil();

    public NetherWart() {
        super("NetherWart", "Automates nether wart harvesting and replanting", Category.WORLD);
    }

    @Override
    public void onEnable() {
        ticker = 0;
        replantTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < delay.get()) return;
        ticker = 0;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();

        if (harvestFirst.isEnabled()) {
            if (tryHarvest(center, r)) return;
            if (replant.isEnabled()) tryReplant(center, r);
        } else {
            if (replant.isEnabled() && tryReplant(center, r)) return;
            tryHarvest(center, r);
        }
    }

    /** Attacks the first fully grown nether wart in range. Returns true if one was found. */
    private boolean tryHarvest(BlockPos center, int r) {
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            var state = mc.world.getBlockState(pos);
            if (!state.isOf(Blocks.NETHER_WART)) continue;
            if (!state.contains(Properties.AGE_3)) continue;
            if (state.get(Properties.AGE_3) < 3) continue;

            mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        }
        return false;
    }

    /** Plants nether wart on the first bare soul sand block in range. Returns true if planted. */
    private boolean tryReplant(BlockPos center, int r) {
        if (!replantTimer.hasReached(400)) return false;

        int wartSlot = findWartSlot();
        if (wartSlot == -1) return false;

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 2, r))) {
            var state    = mc.world.getBlockState(pos);
            var above    = mc.world.getBlockState(pos.up());
            if (!state.isOf(Blocks.SOUL_SAND)) continue;
            if (!above.isAir()) continue;

            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = wartSlot;

            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos.toImmutable(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);

            mc.player.getInventory().selectedSlot = prev;
            replantTimer.reset();
            return true;
        }
        return false;
    }

    private int findWartSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.NETHER_WART)) return i;
        }
        return -1;
    }
}
