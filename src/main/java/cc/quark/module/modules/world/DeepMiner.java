package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class DeepMiner extends Module {
    private final IntSetting length = register(new IntSetting("Length", "Tunnel length in blocks", 50, 5, 200));
    private final BoolSetting placeLight = register(new BoolSetting("Place Torches", "Place torches every 8 blocks", true));
    private final BoolSetting doubleWide = register(new BoolSetting("Double Wide", "Mine 2 blocks wide", false));

    private final TimerUtil timer = new TimerUtil();
    private int mined = 0;
    private Direction facing;

    public DeepMiner() {
        super("Deep Miner", "Auto-tunnel in the direction you're facing", Category.WORLD, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        mined = 0;
        facing = mc.player.getHorizontalFacing();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mined >= length.get()) { disable(); return; }
        if (!timer.hasReached(200)) return;
        timer.reset();

        BlockPos base = mc.player.getBlockPos();
        BlockPos target = base.offset(facing);
        BlockPos targetHigh = target.up();

        if (!mc.world.getBlockState(target).isAir()) {
            mc.interactionManager.attackBlock(target, facing.getOpposite());
        }
        if (!mc.world.getBlockState(targetHigh).isAir()) {
            mc.interactionManager.attackBlock(targetHigh, facing.getOpposite());
        }
        mined++;
    }
}
