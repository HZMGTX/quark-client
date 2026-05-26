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

public class AutoBuild extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Build radius", 3, 1, 8));

    private int ticker = 0;

    public AutoBuild() {
        super("AutoBuild", "Places held blocks to fill a flat platform under you", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < 3) return;
        ticker = 0;

        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        int r = range.get();
        BlockPos base = mc.player.getBlockPos().down();
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                BlockPos pos = base.add(x, 0, z);
                if (!mc.world.getBlockState(pos).isAir()) continue;
                BlockPos support = pos.down();
                if (mc.world.getBlockState(support).isAir()) continue;
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(support), Direction.UP, support, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                return;
            }
        }
    }
}
