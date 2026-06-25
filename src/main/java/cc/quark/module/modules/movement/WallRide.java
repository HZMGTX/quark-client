package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class WallRide extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Sliding speed along walls", 0.15, 0.0, 0.5));

    public WallRide() {
        super("WallRide", "Slide along walls without friction", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isOnGround()) return;

        // Detect adjacent wall block
        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double nx = -Math.sin(yaw);
        double nz = Math.cos(yaw);

        BlockPos sidePos = mc.player.getBlockPos().add((int) Math.round(nx), 0, (int) Math.round(nz));
        boolean adjacentWall = !mc.world.getBlockState(sidePos).isAir();

        if (adjacentWall) {
            Vec3d vel = mc.player.getVelocity();
            // Slow the fall and allow sliding down
            mc.player.setVelocity(vel.x, Math.max(vel.y, -speed.get()), vel.z);
        }
    }
}
