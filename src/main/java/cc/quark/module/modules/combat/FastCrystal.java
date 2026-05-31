package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
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

public class FastCrystal extends Module {

    private final IntSetting minDelay = register(new IntSetting(
            "Min Delay", "Minimum milliseconds between place and pop", 50, 0, 200));

    private final IntSetting range = register(new IntSetting(
            "Range", "Range to target enemies and find crystal positions", 5, 2, 8));

    private final TimerUtil timer = new TimerUtil();
    private boolean justPlaced = false;
    private BlockPos lastPos = null;

    public FastCrystal() {
        super("FastCrystal", "Reduces delay between crystal place and attack", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        justPlaced = false;
        lastPos = null;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;
        if (!timer.hasReached(minDelay.get())) return;

        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        if (!justPlaced) {
            int crystalSlot = findCrystalSlot();
            if (crystalSlot == -1) return;

            BlockPos placePos = findPlacement(target);
            if (placePos == null) return;

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = crystalSlot;

            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(placePos.up()), Direction.UP, placePos, false);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            mc.player.getInventory().selectedSlot = prevSlot;
            lastPos = placePos;
            justPlaced = true;
        } else {
            EndCrystalEntity crystal = findCrystalNear(lastPos);
            if (crystal != null) {
                mc.getNetworkHandler().sendPacket(
                        PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            justPlaced = false;
            lastPos = null;
        }

        timer.reset();
    }

    private BlockPos findPlacement(PlayerEntity target) {
        BlockPos base = target.getBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                BlockPos pos = base.add(dx, -1, dz);
                var block = mc.world.getBlockState(pos).getBlock();
                if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) continue;
                if (!mc.world.getBlockState(pos.up()).isAir()) continue;
                if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos.up())) > range.get()) continue;
                return pos;
            }
        }
        return null;
    }

    private EndCrystalEntity findCrystalNear(BlockPos pos) {
        if (pos == null) return null;
        EndCrystalEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof EndCrystalEntity crystal)) continue;
            double d = e.getPos().distanceTo(Vec3d.ofCenter(pos.up()));
            if (d <= 2.0 && d < bestDist) {
                bestDist = d;
                best = crystal;
            }
        }
        return best;
    }

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) return i;
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
            if (d <= range.get() && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }
}
