package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SnowWalk extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Movement speed on snow blocks (blocks/tick)", 0.2, 0.05, 1.0));

    public SnowWalk() {
        super("SnowWalk", "Cancels snow slow while walking on snow and powder", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        BlockPos below = mc.player.getBlockPos().down();
        var blockBelow = mc.world.getBlockState(below).getBlock();

        boolean onSnow = blockBelow == Blocks.SNOW
                || blockBelow == Blocks.SNOW_BLOCK
                || blockBelow == Blocks.POWDER_SNOW;

        // Also check if the player is inside powder snow
        var blockAt = mc.world.getBlockState(mc.player.getBlockPos()).getBlock();
        boolean inPowder = blockAt == Blocks.POWDER_SNOW;

        if (!onSnow && !inPowder) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double len = Math.sqrt(fwd * fwd + side * side);
        double nFwd = fwd / len;
        double nSide = side / len;

        double dx = (-Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide) * speed.get();
        double dz = (Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide) * speed.get();

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(dx, vel.y, dz);
    }
}
