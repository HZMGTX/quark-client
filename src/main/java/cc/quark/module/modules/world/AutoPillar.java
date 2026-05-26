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

public class AutoPillar extends Module {

    private final IntSetting maxHeight = register(new IntSetting("MaxHeight", "Stop pillaring above this Y", 320, 0, 320));

    public AutoPillar() {
        super("AutoPillar", "Places a block beneath you to pillar upward", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.getBlockPos().getY() >= maxHeight.get()) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) return;
        BlockPos support = below.down();
        if (mc.world.getBlockState(support).isAir()) return;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(support), Direction.UP, support, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
    }
}
