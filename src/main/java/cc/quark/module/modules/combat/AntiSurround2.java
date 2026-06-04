package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AntiSurround2 extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Radius around self to mine blocks", 1.5, 1.0, 3.0));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between mining ticks", 200, 50, 1000));

    private final TimerUtil timer = new TimerUtil();

    public AntiSurround2() {
        super("AntiSurround2", "Enhanced surround counter: mines blocks around self", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        BlockPos playerPos = mc.player.getBlockPos();
        double r = range.get();
        int ri = (int) Math.ceil(r);

        for (Direction dir : Direction.values()) {
            BlockPos candidate = playerPos.offset(dir);
            double dist = Math.sqrt(candidate.getSquaredDistance(playerPos));
            if (dist > r) continue;

            BlockState state = mc.world.getBlockState(candidate);
            if (state.isAir()) continue;
            if (!state.getBlock().getHardness() < 0) { // unbreakable check
                mc.interactionManager.updateBlockBreakingProgress(candidate, dir);
                timer.reset();
                return;
            }
        }

        // Check all positions in range
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -ri; dz <= ri; dz++) {
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    double dist = Math.sqrt(pos.getSquaredDistance(playerPos));
                    if (dist > r) continue;
                    if (pos.equals(playerPos)) continue;

                    BlockState state = mc.world.getBlockState(pos);
                    if (state.isAir()) continue;

                    Direction face = Direction.UP;
                    mc.interactionManager.updateBlockBreakingProgress(pos, face);
                    timer.reset();
                    return;
                }
            }
        }
    }
}
