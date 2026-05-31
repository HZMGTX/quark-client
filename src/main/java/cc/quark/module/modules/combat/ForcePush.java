package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ForcePush extends Module {

    private final BoolSetting useCrystal = register(new BoolSetting(
            "UseCrystal", "Use end crystals to push targets with explosion knockback", true));

    private final TimerUtil timer = new TimerUtil();

    public ForcePush() {
        super("ForcePush", "Uses explosion knockback to push enemies into holes/off edges", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;
        if (!timer.hasReached(200)) return;

        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        if (useCrystal.isEnabled()) {
            crystalPush(target);
        } else {
            directPush(target);
        }

        timer.reset();
    }

    private void crystalPush(PlayerEntity target) {
        int crystalSlot = findSlot(Items.END_CRYSTAL);
        if (crystalSlot == -1) return;

        Vec3d pushDir = target.getPos().subtract(mc.player.getPos()).normalize();
        BlockPos behindTarget = target.getBlockPos().add(
                (int) -Math.round(pushDir.x),
                0,
                (int) -Math.round(pushDir.z));

        var block = mc.world.getBlockState(behindTarget).getBlock();
        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) return;
        if (!mc.world.getBlockState(behindTarget.up()).isAir()) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = crystalSlot;

        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(behindTarget.up()), Direction.UP, behindTarget, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof net.minecraft.entity.decoration.EndCrystalEntity crystal)) continue;
            if (e.getPos().distanceTo(Vec3d.ofCenter(behindTarget.up())) <= 2.0) {
                mc.getNetworkHandler().sendPacket(
                        PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                break;
            }
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private void directPush(PlayerEntity target) {
        Vec3d pushDir = target.getPos().subtract(mc.player.getPos()).normalize();
        target.addVelocity(pushDir.x * 1.5, 0.4, pushDir.z * 1.5);
    }

    private int findSlot(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
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
            if (d <= 6.0 && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
