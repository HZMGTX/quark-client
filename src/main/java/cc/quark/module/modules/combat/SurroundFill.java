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

public class SurroundFill extends Module {

    private final BoolSetting autoRepair = register(new BoolSetting(
            "AutoRepair", "Continuously refill any surround gaps each tick", true));

    public SurroundFill() {
        super("SurroundFill", "Fills gaps in player's surround placement", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!autoRepair.isEnabled()) return;
        if (!mc.player.isOnGround()) return;

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        for (BlockPos pos : getSurroundPositions()) {
            BlockState state = mc.world.getBlockState(pos);
            if (!state.isAir() && !state.isReplaceable()) continue;
            tryPlace(pos);
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private List<BlockPos> getSurroundPositions() {
        BlockPos feet = mc.player.getBlockPos();
        List<BlockPos> list = new ArrayList<>();
        int[][] offsets = {{1,0},{-1,0},{0,1},{0,-1}};
        for (int[] o : offsets) list.add(feet.add(o[0], 0, o[1]));
        return list;
    }

    private void tryPlace(BlockPos target) {
        Direction face = findSupportFace(target);
        if (face == null) return;

        BlockPos neighbor = target.offset(face);
        Vec3d hitVec = Vec3d.ofCenter(target).add(
                face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);

        BlockHitResult hit = new BlockHitResult(hitVec, face.getOpposite(), neighbor, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
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
