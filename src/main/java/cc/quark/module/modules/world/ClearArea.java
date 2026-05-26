package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ClearArea extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Clear radius", 4, 1, 10));

    public ClearArea() {
        super("ClearArea", "Breaks every non-air block within a cube around you", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (mc.world.getBlockState(pos).isAir()) continue;
            mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
            return;
        }
    }
}
