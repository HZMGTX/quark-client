package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoDispenser extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Range to search for dispensers and droppers", 4, 1, 8));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between activations", 1000, 100, 5000));

    private final TimerUtil timer = new TimerUtil();

    public AutoDispenser() {
        super("AutoDispenser", "Automatically activates nearby dispensers and droppers", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int r = range.get();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    var block = mc.world.getBlockState(pos).getBlock();
                    if (block == Blocks.DISPENSER || block == Blocks.DROPPER) {
                        Vec3d hitVec = Vec3d.ofCenter(pos);
                        BlockHitResult hit = new BlockHitResult(hitVec, Direction.NORTH, pos, false);
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                        timer.reset();
                        return;
                    }
                }
            }
        }
    }
}
