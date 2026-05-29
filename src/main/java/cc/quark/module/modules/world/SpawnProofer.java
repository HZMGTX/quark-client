package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SpawnProofer extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Radius around player to scan for dark spots", 8, 2, 16));
    private final IntSetting lightThreshold = register(new IntSetting(
            "Light Threshold", "Place light when block light level is below this", 8, 1, 15));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between placements", 300, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public SpawnProofer() {
        super("SpawnProofer", "Places torches in dark areas to prevent mob spawning", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        int torchSlot = findLightSourceSlot();
        if (torchSlot == -1) return;

        BlockPos center = mc.player.getBlockPos();
        int r = range.get();

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = center.add(x, y, z);
                    if (!mc.world.getBlockState(pos).isAir()) continue;

                    BlockPos below = pos.down();
                    if (mc.world.getBlockState(below).isAir()) continue;
                    if (!mc.world.getBlockState(below).isSolidBlock(mc.world, below)) continue;

                    int light = mc.world.getLightLevel(pos);
                    if (light >= lightThreshold.get()) continue;

                    // Place torch here
                    int saved = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = torchSlot;

                    Vec3d hitVec = Vec3d.ofCenter(below).add(0, 0.5, 0);
                    BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, below, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.player.swingHand(Hand.MAIN_HAND);

                    mc.player.getInventory().selectedSlot = saved;
                    timer.reset();
                    return;
                }
            }
        }
    }

    private int findLightSourceSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            var item = mc.player.getInventory().getStack(i).getItem();
            if (item == Items.TORCH || item == Items.GLOWSTONE || item == Items.SEA_LANTERN
                    || item == Items.LANTERN || item == Items.SOUL_TORCH) return i;
        }
        return -1;
    }
}
