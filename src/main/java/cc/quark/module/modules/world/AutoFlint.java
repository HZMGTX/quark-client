package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoFlint extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Radius to search for gravel", 4, 1, 8));
    private final IntSetting maxBreaks = register(new IntSetting("MaxBreaks", "Max gravel blocks to break per session", 32, 1, 128));

    private final TimerUtil timer = new TimerUtil();
    private int brokenCount = 0;

    public AutoFlint() {
        super("AutoFlint", "Automatically breaks gravel to collect flint", Category.WORLD);
    }

    @Override
    public void onEnable() {
        brokenCount = 0;
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (brokenCount >= maxBreaks.get()) {
            setEnabled(false);
            return;
        }
        if (!timer.hasReached(100)) return;
        timer.reset();

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.GRAVEL)) continue;

            mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
            brokenCount++;
            return;
        }
    }
}
