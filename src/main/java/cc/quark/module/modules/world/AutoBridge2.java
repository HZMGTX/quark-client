package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBridge2 extends Module {

    private final BoolSetting sneak = register(new BoolSetting("Sneak", "Place behind facing direction", true));

    public AutoBridge2() {
        super("AutoBridge2", "Bridges blocks in the direction you face", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        Direction facing = mc.player.getHorizontalFacing();
        BlockPos below = mc.player.getBlockPos().down();
        BlockPos target = sneak.isEnabled() ? below.offset(facing.getOpposite()) : below.offset(facing);
        if (!mc.world.getBlockState(target).isAir()) return;

        BlockPos support = below;
        if (mc.world.getBlockState(support).isAir()) return;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(support), Direction.UP, support, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
    }
}
