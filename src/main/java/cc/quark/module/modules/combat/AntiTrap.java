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

public class AntiTrap extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Detection radius for trap blocks around self", 3.0, 1.0, 5.0));

    private final IntSetting breakDelay = register(new IntSetting(
            "Break Delay", "Milliseconds between block break attempts", 150, 50, 1000));

    private final TimerUtil timer = new TimerUtil();

    public AntiTrap() {
        super("AntiTrap", "Detects and breaks traps (obsidian cages, etc.)", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(breakDelay.get())) return;

        BlockPos playerPos = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());

        // Prioritize blocks immediately adjacent to player (cage detection)
        for (Direction dir : Direction.values()) {
            BlockPos adjacent = playerPos.offset(dir);
            BlockState state = mc.world.getBlockState(adjacent);
            if (state.isAir()) continue;

            float hardness = state.getHardness(mc.world, adjacent);
            // Skip unbreakable blocks (bedrock, barrier)
            if (hardness < 0) continue;

            mc.interactionManager.updateBlockBreakingProgress(adjacent, dir.getOpposite());
            timer.reset();
            return;
        }

        // Also check slightly larger radius for cage structures
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    BlockPos pos = playerPos.add(dx, dy, dz);
                    double dist = mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    if (dist > range.get() * range.get()) continue;

                    BlockState state = mc.world.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (state.getHardness(mc.world, pos) < 0) continue;

                    mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
                    timer.reset();
                    return;
                }
            }
        }
    }
}
