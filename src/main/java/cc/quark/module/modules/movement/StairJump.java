package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * StairJump - Automatically jumps when colliding horizontally with a stair block.
 */
public class StairJump extends Module {

    private final BoolSetting sprintBoost = register(new BoolSetting("SprintBoost", "Add extra horizontal velocity on stair jump", true));

    public StairJump() {
        super("StairJump", "Auto-jumps when hitting stair blocks while moving forward", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (!mc.player.horizontalCollision) return;

        // Check if moving forward
        float fwd = mc.player.input.movementForward;
        if (fwd <= 0) return;

        // Check feet and head level for stair blocks
        BlockPos feetPos = mc.player.getBlockPos();
        BlockPos headPos = feetPos.up();

        boolean stairAtFeet = mc.world.getBlockState(feetPos).getBlock() instanceof StairsBlock;
        boolean stairAhead = isStairAhead();

        if (stairAtFeet || stairAhead) {
            Vec3d vel = mc.player.getVelocity();
            double jumpY = 0.42;

            if (sprintBoost.isEnabled() && mc.player.isSprinting()) {
                double yawRad = Math.toRadians(mc.player.getYaw());
                double boostX = -Math.sin(yawRad) * 0.2;
                double boostZ =  Math.cos(yawRad) * 0.2;
                mc.player.setVelocity(vel.x + boostX, jumpY, vel.z + boostZ);
            } else {
                mc.player.setVelocity(vel.x, jumpY, vel.z);
            }
        }
    }

    private boolean isStairAhead() {
        if (mc.player == null || mc.world == null) return false;
        double yawRad = Math.toRadians(mc.player.getYaw());
        double dx = -Math.sin(yawRad);
        double dz =  Math.cos(yawRad);

        BlockPos ahead = BlockPos.ofFloored(
                mc.player.getX() + dx,
                mc.player.getY(),
                mc.player.getZ() + dz
        );
        return mc.world.getBlockState(ahead).getBlock() instanceof StairsBlock;
    }
}
