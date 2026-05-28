package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class EdgeStep extends Module {

    private final DoubleSetting stepHeight = register(new DoubleSetting(
            "Step Height", "Max step-up height in blocks", 1.0, 0.6, 2.5));

    public EdgeStep() {
        super("EdgeStep", "Step up blocks while sprinting", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (!isMoving()) return;

        double yaw = Math.toRadians(mc.player.getYaw());
        double dx = -Math.sin(yaw);
        double dz = Math.cos(yaw);

        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();

        BlockPos feetPos = BlockPos.ofFloored(px + dx * 0.6, py, pz + dz * 0.6);

        BlockState feetBlock = mc.world.getBlockState(feetPos);
        if (feetBlock.isAir()) return;

        BlockPos headPos = feetPos.up();
        BlockState headBlock = mc.world.getBlockState(headPos);
        if (!headBlock.isAir()) return;

        VoxelShape shape = feetBlock.getCollisionShape(mc.world, feetPos);
        if (shape.isEmpty()) return;

        double blockTop = feetPos.getY() + shape.getMax(net.minecraft.util.math.Direction.Axis.Y);
        double stepDiff = blockTop - py;

        if (stepDiff > 0 && stepDiff <= stepHeight.get()) {
            Vec3d vel = mc.player.getVelocity();
            double upVel = 0.1 + stepDiff * 0.25;
            mc.player.setVelocity(vel.x, Math.max(vel.y, upVel), vel.z);
        }
    }

    private boolean isMoving() {
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }
}
