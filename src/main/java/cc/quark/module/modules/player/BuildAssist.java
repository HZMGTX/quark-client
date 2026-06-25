package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BuildAssist extends Module {

    private final BoolSetting autoFill = register(new BoolSetting(
            "Auto Fill", "Automatically place blocks to fill gaps under player's feet", true));

    private final DoubleSetting reach = register(new DoubleSetting(
            "Reach", "Block placement reach distance", 4.5, 2.0, 6.0));

    public BuildAssist() {
        super("BuildAssist", "Assists with building by auto-placing support blocks", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (!autoFill.isEnabled()) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.isOnGround()) return;

        // Find block below feet
        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) return;

        // Need to reach (below.down()) to place on top of it
        BlockPos support = below.down();
        if (mc.player.getPos().distanceTo(support.toCenterPos()) > reach.get()) return;

        BlockState supportState = mc.world.getBlockState(support);
        if (supportState.isAir()) return; // need a surface to place on

        // Find a block in hotbar
        int blockSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                blockSlot = i;
                break;
            }
        }
        if (blockSlot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        BlockHitResult hitResult = new BlockHitResult(
                support.toCenterPos().add(0, 0.5, 0),
                Direction.UP,
                support,
                false);

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.getInventory().selectedSlot = prev;
    }
}
