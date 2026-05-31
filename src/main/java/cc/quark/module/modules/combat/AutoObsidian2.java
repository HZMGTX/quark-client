package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoObsidian2 extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between obsidian placements", 100, 0, 1000));

    private final TimerUtil timer = new TimerUtil();

    public AutoObsidian2() {
        super("AutoObsidian2", "Places obsidian from inventory at feet automatically", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        int obsSlot = findObsidianSlot();
        if (obsSlot == -1) return;

        BlockPos feetPos = mc.player.getBlockPos();
        BlockPos below = feetPos.down();

        BlockState belowState = mc.world.getBlockState(below);
        if (!belowState.isAir() && !belowState.isReplaceable()) return;

        Direction face = findSupportFace(below);
        if (face == null) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = obsSlot;

        BlockPos neighbor = below.offset(face);
        Vec3d hitVec = Vec3d.ofCenter(below).add(
                face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);

        BlockHitResult hit = new BlockHitResult(hitVec, face.getOpposite(), neighbor, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        mc.player.getInventory().selectedSlot = prevSlot;
        timer.reset();
    }

    private Direction findSupportFace(BlockPos target) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (!state.isAir() && !state.isReplaceable() && state.isSolidBlock(mc.world, neighbor)) return dir;
        }
        return null;
    }

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.OBSIDIAN)) return i;
        }
        return -1;
    }
}
