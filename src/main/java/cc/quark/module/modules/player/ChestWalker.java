package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ChestWalker extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Radius to search for chests", 8.0, 1.0, 20.0));
    private final BoolSetting autoOpen = register(new BoolSetting(
            "AutoOpen", "Automatically open chest when adjacent", true));

    private final TimerUtil timer = new TimerUtil();

    public ChestWalker() {
        super("ChestWalker", "Auto-walks to nearby chests", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(50)) return;
        timer.reset();

        BlockPos chestPos = findNearestChest();
        if (chestPos == null) return;

        Vec3d chestCenter = Vec3d.ofCenter(chestPos);
        double dist = mc.player.getPos().distanceTo(chestCenter);

        if (dist <= 2.5) {
            if (autoOpen.isEnabled()) {
                BlockHitResult hit = new BlockHitResult(chestCenter, Direction.UP, chestPos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            }
            return;
        }

        // Step toward chest
        Vec3d playerPos = mc.player.getPos();
        Vec3d dir = chestCenter.subtract(playerPos).normalize().multiply(0.25);

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                playerPos.x + dir.x,
                playerPos.y,
                playerPos.z + dir.z,
                mc.player.isOnGround()));
    }

    private BlockPos findNearestChest() {
        double rangeSq = range.get() * range.get();
        int r = (int) Math.ceil(range.get());
        BlockPos center = mc.player.getBlockPos();
        BlockPos nearest = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            double dSq = pos.getSquaredDistance(mc.player.getPos());
            if (dSq > rangeSq) continue;
            var block = mc.world.getBlockState(pos).getBlock();
            if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.BARREL) {
                if (dSq < bestDist) {
                    bestDist = dSq;
                    nearest = pos.toImmutable();
                }
            }
        }
        return nearest;
    }
}
