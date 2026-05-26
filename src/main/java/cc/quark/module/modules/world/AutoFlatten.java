package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class AutoFlatten extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Flatten radius", 4, 1, 12));

    public AutoFlatten() {
        super("AutoFlatten", "Breaks all blocks above your feet level to flatten terrain", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        int r = range.get();
        int baseY = mc.player.getBlockPos().getY();
        BlockPos center = mc.player.getBlockPos();
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = 0; y <= 3; y++) {
                    BlockPos pos = new BlockPos(center.getX() + x, baseY + y, center.getZ() + z);
                    if (mc.world.getBlockState(pos).isAir()) continue;
                    mc.interactionManager.attackBlock(pos, Direction.UP);
                    return;
                }
            }
        }
    }
}
