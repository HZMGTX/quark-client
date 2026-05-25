package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.AirBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class Excavator extends Module {

    private final ModeSetting shape = register(new ModeSetting(
            "Shape", "Excavation pattern", "Tunnel", "Tunnel", "Room", "Staircase"));
    private final IntSetting width = register(new IntSetting(
            "Width", "Tunnel half-width (1 = 3 wide, 2 = 5 wide)", 1, 1, 4));
    private final IntSetting height = register(new IntSetting(
            "Height", "Tunnel height in blocks", 2, 1, 4));
    private final IntSetting reach = register(new IntSetting(
            "Depth", "How many blocks ahead to mine", 3, 1, 6));

    private int ticker = 0;

    public Excavator() {
        super("Excavator", "Mines a tunnel or room in the direction you face", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < 4) return;
        ticker = 0;

        BlockPos origin  = mc.player.getBlockPos();
        Direction facing = mc.player.getHorizontalFacing();
        int w = width.get();
        int h = height.get();
        int d = reach.get();

        for (int fw = 1; fw <= d; fw++) {
            for (int dx = -w; dx <= w; dx++) {
                for (int dy = 0; dy < h; dy++) {
                    BlockPos target = origin
                            .offset(facing, fw)
                            .offset(facing.rotateYClockwise(), dx)
                            .up(dy);
                    if (mc.world.getBlockState(target).getBlock() instanceof AirBlock) continue;
                    mc.interactionManager.attackBlock(target, Direction.UP);
                    return;
                }
            }
        }
    }
}
