package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class HolePush extends Module {

    private final BoolSetting useWeb = register(new BoolSetting(
            "UseWeb", "Place cobwebs to slow enemies in their hole", true));

    private final TimerUtil timer = new TimerUtil();

    public HolePush() {
        super("HolePush", "Pushes nearest enemy out of their hole using web/push", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;

        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        BlockPos targetFeet = target.getBlockPos();
        BlockState feetState = mc.world.getBlockState(targetFeet);

        if (useWeb.isEnabled()) {
            int webSlot = findItemSlot(Items.COBWEB);
            if (webSlot != -1 && feetState.isAir()) {
                placeBlock(targetFeet, webSlot);
                timer.reset();
                return;
            }
        }

        Vec3d pushDir = target.getPos().subtract(mc.player.getPos()).normalize();
        target.addVelocity(pushDir.x * 0.5, 0.3, pushDir.z * 0.5);
        timer.reset();
    }

    private void placeBlock(BlockPos pos, int slot) {
        Direction face = findSupportFace(pos);
        if (face == null) return;

        BlockPos neighbor = pos.offset(face);
        Vec3d hitVec = Vec3d.ofCenter(pos).add(
                face.getOffsetX() * 0.5,
                face.getOffsetY() * 0.5,
                face.getOffsetZ() * 0.5);

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        BlockHitResult hit = new BlockHitResult(hitVec, face.getOpposite(), neighbor, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private Direction findSupportFace(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            BlockState state = mc.world.getBlockState(neighbor);
            if (!state.isAir() && state.isSolidBlock(mc.world, neighbor)) return dir;
        }
        return null;
    }

    private int findItemSlot(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(item)) return i;
        }
        return -1;
    }

    private PlayerEntity findNearestTarget() {
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestDist && d <= 6.0) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
