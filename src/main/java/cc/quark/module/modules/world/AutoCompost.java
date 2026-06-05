package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoCompost extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Delay between compost actions (ms)", 150, 50, 1000));

    private final TimerUtil timer = new TimerUtil();

    public AutoCompost() {
        super("AutoCompost", "Automatically deposits compostable items into nearby composters", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Check if player is looking at a composter
        BlockPos target = null;
        var hit = mc.crosshairTarget;
        if (hit instanceof net.minecraft.util.hit.BlockHitResult bhr) {
            BlockPos pos = bhr.getBlockPos();
            if (mc.world.getBlockState(pos).isOf(Blocks.COMPOSTER)) {
                target = pos;
            }
        }

        // Also search nearby for composters
        if (target == null) {
            BlockPos center = mc.player.getBlockPos();
            for (BlockPos pos : BlockPos.iterate(center.add(-2, -1, -2), center.add(2, 1, 2))) {
                if (mc.world.getBlockState(pos).isOf(Blocks.COMPOSTER)) {
                    target = pos.toImmutable();
                    break;
                }
            }
        }

        if (target == null) return;

        // Find a compostable item in hotbar
        int slot = findCompostableSlot();
        if (slot < 0) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(target).add(0, 0.5, 0), Direction.UP, target, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = prevSlot;
        timer.reset();
    }

    private int findCompostableSlot() {
        if (mc.player == null) return -1;
        var inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (isCompostable(inv.getStack(i).getItem())) return i;
        }
        for (int i = 9; i < 36; i++) {
            if (isCompostable(inv.getStack(i).getItem())) return i;
        }
        return -1;
    }

    private boolean isCompostable(Item item) {
        return item == Items.WHEAT_SEEDS
                || item == Items.BEETROOT_SEEDS
                || item == Items.MELON_SEEDS
                || item == Items.PUMPKIN_SEEDS
                || item == Items.DRIED_KELP
                || item == Items.OAK_LEAVES
                || item == Items.SHORT_GRASS
                || item == Items.FERN
                || item == Items.WHEAT
                || item == Items.CARROT
                || item == Items.POTATO
                || item == Items.BEETROOT
                || item == Items.APPLE
                || item == Items.MELON_SLICE
                || item == Items.KELP
                || item == Items.SUGAR_CANE
                || item == Items.BAMBOO
                || item == Items.VINE;
    }
}
