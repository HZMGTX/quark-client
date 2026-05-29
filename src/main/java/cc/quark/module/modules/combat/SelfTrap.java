package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

/**
 * SelfTrap — places obsidian on all 4 cardinal sides of the player when HP
 * drops below the threshold.
 * Mode 1Layer places at player-feet level; 2Layer also fills the layer above.
 */
public class SelfTrap extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting("Threshold", "HP at or below which to trap",  8.0,  1.0, 20.0));
    private final ModeSetting   mode      = register(new ModeSetting  ("Mode",      "Layers to place",               "1Layer", "1Layer", "2Layer"));
    private final IntSetting    delayMs   = register(new IntSetting   ("Delay",     "Ms between trap activations",   500, 100, 5000));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    private static final Direction[] SIDES = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST
    };

    public SelfTrap() {
        super("SelfTrap", "Places obsidian around the player when HP is low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getHealth() > threshold.get()) { restoreSlot(); return; }
        if (!timer.hasReached(delayMs.get())) return;

        // Find obsidian in hotbar
        int obsSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.OBSIDIAN)) { obsSlot = i; break; }
        }
        if (obsSlot == -1) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != obsSlot) { prevSlot = cur; mc.player.getInventory().selectedSlot = obsSlot; }

        BlockPos feet = mc.player.getBlockPos();
        int layers = mode.is("2Layer") ? 2 : 1;

        for (Direction side : SIDES) {
            for (int dy = 0; dy < layers; dy++) {
                BlockPos pos = feet.offset(side).up(dy);
                if (!mc.world.getBlockState(pos).isAir()) continue;
                Vec3d hitVec = Vec3d.ofCenter(pos.offset(side.getOpposite())).add(
                        side.getOffsetX() * 0.5, 0, side.getOffsetZ() * 0.5);
                BlockHitResult hit = new BlockHitResult(hitVec, side.getOpposite(), pos.offset(side.getOpposite()), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            }
        }

        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
        restoreSlot();
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
