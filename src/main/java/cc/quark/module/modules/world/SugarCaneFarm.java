package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SugarCaneFarm extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Scan radius", 5, 1, 12));

    private int ticker = 0;

    public SugarCaneFarm() {
        super("SugarCaneFarm", "Breaks tall sugar cane while leaving the base intact", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < 3) return;
        ticker = 0;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -2, -r), center.add(r, 4, r))) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.SUGAR_CANE)) continue;
            if (!mc.world.getBlockState(pos.down()).isOf(Blocks.SUGAR_CANE)) continue;
            mc.interactionManager.attackBlock(pos.toImmutable(), Direction.UP);
            return;
        }
    }
}
