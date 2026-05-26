package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class FillArea extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Fill radius", 3, 1, 8));

    public FillArea() {
        super("FillArea", "Fills air gaps around you with the held block", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        int r = range.get();
        BlockPos center = mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (!mc.world.getBlockState(pos).isAir()) continue;
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.offset(dir);
                if (mc.world.getBlockState(neighbor).isAir()) continue;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(neighbor), dir.getOpposite(), neighbor.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                return;
            }
        }
    }
}
