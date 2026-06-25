package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class NetherHighwayHelper extends Module {
    private final ModeSetting direction = register(new ModeSetting("Direction", "Highway direction", "North", "North", "South", "East", "West"));
    private final BoolSetting fillFloor = register(new BoolSetting("Fill Floor", "Fill floor with obsidian", true));
    private final BoolSetting clearPath = register(new BoolSetting("Clear Path", "Mine blocks in path", true));
    private final TimerUtil timer = new TimerUtil();

    public NetherHighwayHelper() {
        super("Nether Highway", "Assists building nether highways", Category.WORLD, 0);
    }

    @Override
    public void onEnable() {
        ChatUtil.info("[NetherHighway] Building highway facing " + direction.get() + ". Walk forward to continue.");
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(100)) return;
        timer.reset();

        BlockPos pos = mc.player.getBlockPos();

        if (clearPath.isEnabled()) {
            Direction facing = getFacing();
            BlockPos front = pos.offset(facing);
            if (!mc.world.getBlockState(front).isAir() && mc.world.getBlockState(front).getBlock() != Blocks.BEDROCK) {
                mc.interactionManager.attackBlock(front, facing.getOpposite());
            }
        }
    }

    private Direction getFacing() {
        return switch (direction.get()) {
            case "South" -> Direction.SOUTH;
            case "East" -> Direction.EAST;
            case "West" -> Direction.WEST;
            default -> Direction.NORTH;
        };
    }
}
