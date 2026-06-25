package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Surround2 extends Module {

    private final BoolSetting smart = register(new BoolSetting(
            "Smart", "Only place blocks in positions that are currently missing (skip filled spots)", true));

    private final BoolSetting autoToggle = register(new BoolSetting(
            "Auto Toggle", "Automatically disable after the full surround is placed", false));

    private static final int[][] OFFSETS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    public Surround2() {
        super("Surround2", "Places obsidian blocks around the player's feet for crystal protection", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            BlockPos feet = mc.player.getBlockPos();
            mc.player.setPosition(feet.getX() + 0.5, mc.player.getY(), feet.getZ() + 0.5);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.player.isOnGround()) return;

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        BlockPos feet = mc.player.getBlockPos();
        boolean allFilled = true;
        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        for (int[] off : OFFSETS) {
            BlockPos target = feet.add(off[0], 0, off[1]);
            if (!mc.world.getBlockState(target).isAir()) continue;
            allFilled = false;
            if (smart.isEnabled()) {
                placeAt(target);
            } else {
                placeAt(target);
            }
        }

        mc.player.getInventory().selectedSlot = prev;

        if (autoToggle.isEnabled() && allFilled) {
            disable();
        }
    }

    private void placeAt(BlockPos target) {
        Direction face = findSupportFace(target);
        if (face == null) return;
        BlockPos neighbor = target.offset(face);
        Vec3d hitVec = Vec3d.ofCenter(target).add(
                face.getOffsetX() * 0.5,
                face.getOffsetY() * 0.5,
                face.getOffsetZ() * 0.5);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                new BlockHitResult(hitVec, face.getOpposite(), neighbor, false));
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }

    private Direction findSupportFace(BlockPos target) {
        for (Direction dir : Direction.values()) {
            BlockPos nb = target.offset(dir);
            var state = mc.world.getBlockState(nb);
            if (!state.isAir() && !state.isReplaceable() && state.isSolidBlock(mc.world, nb)) {
                return dir;
            }
        }
        return null;
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!s.isEmpty() && s.getItem() instanceof BlockItem && s.isOf(Items.OBSIDIAN)) return i;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!s.isEmpty() && s.getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
}
