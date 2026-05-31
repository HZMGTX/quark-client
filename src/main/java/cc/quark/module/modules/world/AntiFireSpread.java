package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class AntiFireSpread extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Radius to remove fire blocks", 8, 1, 20));

    public AntiFireSpread() {
        super("AntiFireSpread", "Removes fire blocks near you each tick to prevent fire spread", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        BlockPos center = mc.player.getBlockPos();
        int r = radius.get();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (mc.world.getBlockState(pos).getBlock() == Blocks.FIRE
                    || mc.world.getBlockState(pos).getBlock() == Blocks.SOUL_FIRE) {
                mc.world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }
}
