package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoDoor extends Module {

    private final BoolSetting gates     = register(new BoolSetting("Gates",     "Auto-open/close fence gates",  true));
    private final BoolSetting trapdoors = register(new BoolSetting("Trapdoors", "Auto-open/close trapdoors",    false));

    private final TimerUtil timer = new TimerUtil();

    public AutoDoor() {
        super("AutoDoor", "Automatically opens and closes doors, gates, and trapdoors", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(100)) return;
        timer.reset();

        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-3, -1, -3), center.add(3, 3, 3))) {
            var state = mc.world.getBlockState(pos);
            var block = state.getBlock();

            boolean isDoor     = block instanceof DoorBlock;
            boolean isGate     = gates.isEnabled()     && block instanceof FenceGateBlock;
            boolean isTrapdoor = trapdoors.isEnabled() && block instanceof TrapdoorBlock;

            if (!isDoor && !isGate && !isTrapdoor) continue;
            if (!state.contains(Properties.OPEN)) continue;

            if (isDoor && state.contains(Properties.DOUBLE_BLOCK_HALF)) {
                if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) continue;
            }

            boolean open = state.get(Properties.OPEN);
            double dist  = mc.player.squaredDistanceTo(Vec3d.ofCenter(pos));

            if (!open && dist < 4.0) {
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
            } else if (open && dist > 9.0 && dist < 25.0) {
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}
