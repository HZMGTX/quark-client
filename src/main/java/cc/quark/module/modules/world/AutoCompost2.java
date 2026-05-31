package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoCompost2 extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Delay between compost actions (ms)", 200, 50, 1000));

    private final TimerUtil timer = new TimerUtil();

    public AutoCompost2() {
        super("AutoCompost2", "Puts compostable items into composter and takes bonemeal when ready", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-3, -1, -3), center.add(3, 1, 3))) {
            var state = mc.world.getBlockState(pos);
            if (!state.isOf(Blocks.COMPOSTER)) continue;
            BlockPos immutable = pos.toImmutable();

            int level = state.get(ComposterBlock.LEVEL);

            if (level == 8) {
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, immutable, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
                timer.reset();
                return;
            }

            int slot = findCompostableSlot();
            if (slot < 0) return;

            int saved = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, immutable, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = saved;
            timer.reset();
            return;
        }
    }

    private int findCompostableSlot() {
        if (mc.player == null) return -1;
        var inv = mc.player.getInventory();
        for (int i = 0; i < 36; i++) {
            if (isCompostable(inv.getStack(i).getItem())) return i;
        }
        return -1;
    }

    private boolean isCompostable(Item item) {
        return item == Items.WHEAT_SEEDS || item == Items.BEETROOT_SEEDS
                || item == Items.MELON_SEEDS || item == Items.PUMPKIN_SEEDS
                || item == Items.DRIED_KELP || item == Items.WHEAT
                || item == Items.CARROT || item == Items.POTATO
                || item == Items.BEETROOT || item == Items.APPLE
                || item == Items.MELON_SLICE || item == Items.KELP
                || item == Items.SUGAR_CANE || item == Items.BAMBOO
                || item == Items.VINE || item == Items.GRASS;
    }
}
