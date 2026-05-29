package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBridge extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Bridging mode",
            "Normal", "Normal", "SafeWalk"));
    private final BoolSetting onlyWhenBack = register(new BoolSetting(
            "Only Backward", "Only place when moving backward", true));

    public AutoBridge() {
        super("AutoBridge", "Automatically places blocks beneath feet while bridging", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Must be holding a block item
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        // Check movement direction restriction
        if (onlyWhenBack.isEnabled() && mc.player.input.movementForward >= 0) return;

        // In SafeWalk mode, prevent the player from falling off edges
        if (mode.is("SafeWalk")) {
            BlockPos belowFeet = mc.player.getBlockPos().down();
            if (!mc.world.getBlockState(belowFeet).isAir()) return; // already has block below
        }

        // Try to place a block at our feet position
        BlockPos below = mc.player.getBlockPos().down();
        BlockState stateBelow = mc.world.getBlockState(below);
        if (!stateBelow.isAir()) return; // block already there

        // Find a solid adjacent block to place against
        for (Direction dir : Direction.values()) {
            BlockPos adjacent = below.offset(dir);
            BlockState adjacentState = mc.world.getBlockState(adjacent);
            if (adjacentState.isAir()) continue;

            Vec3d hitVec = Vec3d.ofCenter(adjacent).add(
                    dir.getOpposite().getOffsetX() * 0.5,
                    dir.getOpposite().getOffsetY() * 0.5,
                    dir.getOpposite().getOffsetZ() * 0.5);

            BlockHitResult hitResult = new BlockHitResult(hitVec, dir.getOpposite(), adjacent, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            break;
        }
    }
}
