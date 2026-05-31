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

public class CrystalPhase extends Module {

    private final IntSetting placeDelay = register(new IntSetting(
            "Place Delay", "Milliseconds between place and pop cycles", 50, 0, 300));

    private final IntSetting range = register(new IntSetting(
            "Range", "Target range in blocks", 5, 2, 8));

    private final TimerUtil timer = new TimerUtil();
    private boolean placing = true;

    public CrystalPhase() {
        super("CrystalPhase", "Places and pops end crystals in rapid phase sequence", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        placing = true;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;
        if (!timer.hasReached(placeDelay.get())) return;

        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        if (placing) {
            int crystalSlot = findCrystalSlot();
            if (crystalSlot == -1) return;

            BlockPos placePos = findPlacementNear(target);
            if (placePos == null) return;

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = crystalSlot;

            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(placePos.up()), Direction.UP, placePos, false);
            mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hit, 0));
            mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));

            mc.player.getInventory().selectedSlot = prevSlot;
            placing = false;
        } else {
            EndCrystalEntity crystal = findNearbyCrystal(target);
            if (crystal != null) {
                mc.getNetworkHandler().sendPacket(
                        PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            placing = true;
        }

        timer.reset();
    }

    private BlockPos findPlacementNear(PlayerEntity target) {
        BlockPos base = target.getBlockPos();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos pos = base.add(dx, dy, dz);
                    var block = mc.world.getBlockState(pos).getBlock();
                    if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) continue;
                    if (!mc.world.getBlockState(pos.up()).isAir()) continue;
                    if (mc.player.getEyePos().distanceTo(Vec3d.ofCenter(pos.up())) > range.get()) continue;
                    return pos;
                }
            }
        }
        return null;
    }

    private EndCrystalEntity findNearbyCrystal(PlayerEntity target) {
        EndCrystalEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof EndCrystalEntity crystal)) continue;
            double d = mc.player.distanceTo(e);
            if (d <= range.get() && d < bestDist) {
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
