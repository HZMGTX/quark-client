package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoParkour extends Module {

    private final BoolSetting predict = register(new BoolSetting(
            "Predict", "Look ahead multiple blocks to predict jump timing", true));
    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Keep sprinting automatically while active", true));

    public AutoParkour() {
        super("AutoParkour", "Automatically times jumps at the edge of blocks for parkour", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isSneaking()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        if (sprint.isEnabled()) mc.player.setSprinting(true);

        Vec3d vel = mc.player.getVelocity();
        double vx = vel.x, vz = vel.z;
        double hLen = Math.sqrt(vx * vx + vz * vz);

        if (hLen < 0.01) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            vx = -Math.sin(yaw);
            vz =  Math.cos(yaw);
            hLen = 1.0;
        }

        double nx = vx / hLen;
        double nz = vz / hLen;

        int lookRange = predict.isEnabled() ? 3 : 1;

        for (int i = 1; i <= lookRange; i++) {
            double checkX = mc.player.getX() + nx * i;
            double checkZ = mc.player.getZ() + nz * i;
            double checkY = mc.player.getY();

            BlockPos aheadBelow = new BlockPos(
                    (int) Math.floor(checkX),
                    (int) Math.floor(checkY) - 1,
                    (int) Math.floor(checkZ));

            if (mc.world.getBlockState(aheadBelow).isAir()) {
                BlockPos currentBelow = new BlockPos(
                        (int) Math.floor(mc.player.getX()),
                        (int) Math.floor(checkY) - 1,
                        (int) Math.floor(mc.player.getZ()));
                if (!mc.world.getBlockState(currentBelow).isAir()) {
                    mc.player.jump();
                }
                break;
            }
        }
    }
}
