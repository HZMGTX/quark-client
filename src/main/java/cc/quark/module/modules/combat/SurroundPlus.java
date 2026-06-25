package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SurroundPlus extends Module {

    private final BoolSetting fullSurround = register(new BoolSetting(
            "FullSurround", "Also covers diagonal positions for complete protection", true));

    private final BoolSetting center = register(new BoolSetting(
            "Center", "Snap player to block center before placing", true));

    public SurroundPlus() {
        super("SurroundPlus", "Extended surround that also covers diagonal blocks", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) return;
        if (center.isEnabled()) {
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

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        for (BlockPos target : buildPositions()) {
            tryPlace(target);
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private List<BlockPos> buildPositions() {
        BlockPos feet = mc.player.getBlockPos();
        List<BlockPos> result = new ArrayList<>();

        int[][] cardinals = {{0,1},{0,-1},{1,0},{-1,0}};
        int[][] diagonals = {{1,1},{-1,1},{1,-1},{-1,-1}};

        for (int[] o : cardinals) result.add(feet.add(o[0], 0, o[1]));
        if (fullSurround.isEnabled()) {
            for (int[] o : diagonals) result.add(feet.add(o[0], 0, o[1]));
        }
        return result;
    }

    private void tryPlace(BlockPos target) {
        BlockState existing = mc.world.getBlockState(target);
        if (!existing.isAir() && !existing.isReplaceable()) return;

        Direction face = findSupportFace(target);
        if (face == null) return;

        BlockPos neighbor = target.offset(face);
        Vec3d hitVec = Vec3d.ofCenter(target).add(
                face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);

        BlockHitResult hitResult = new BlockHitResult(hitVec, face.getOpposite(), neighbor, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
    }

    private Direction findSupportFace(BlockPos target) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (!state.isAir() && !state.isReplaceable() && state.isSolidBlock(mc.world, neighbor)) return dir;
        }
        return null;
    }

    private int findBlockSlot() {
        int fallback = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) continue;
            if (stack.isOf(Items.OBSIDIAN)) return i;
            if (fallback == -1) fallback = i;
        }
        return fallback;
    }
}
