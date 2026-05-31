package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BedBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AutoSleep extends Module {

    private final BoolSetting autoWake = register(new BoolSetting(
            "AutoWake", "Wake up automatically at dawn", true));

    private final TimerUtil timer = new TimerUtil();

    public AutoSleep() {
        super("AutoSleep", "Sleeps in nearest bed when it's nighttime", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(1000)) return;
        timer.reset();

        ClientPlayerEntity player = mc.player;
        World world = mc.world;

        if (player.isSleeping()) {
            if (autoWake.isEnabled() && !world.isNight()) {
                player.wakeUp();
            }
            return;
        }

        if (!world.isNight()) return;

        BlockPos nearest = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.iterate(
                (int) player.getX() - 6, (int) player.getY() - 2, (int) player.getZ() - 6,
                (int) player.getX() + 6, (int) player.getY() + 2, (int) player.getZ() + 6)) {
            if (world.getBlockState(pos).getBlock() instanceof BedBlock) {
                double dist = player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                if (dist < bestDist) {
                    bestDist = dist;
                    nearest = pos.toImmutable();
                }
            }
        }

        if (nearest == null) return;

        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(nearest),
                net.minecraft.util.math.Direction.UP,
                nearest,
                false);
        mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hit);
    }
}
