package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class AntiWall extends Module {

    private final BoolSetting autoStrafe = register(new BoolSetting("AutoStrafe", "Strafe around walls automatically", true));

    public AntiWall() {
        super("AntiWall", "Detects upcoming wall collision and slides around it", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isOnGround()) return;

        Vec3d vel = mc.player.getVelocity();
        if (Math.abs(vel.x) < 0.01 && Math.abs(vel.z) < 0.01) return;

        Vec3d pos = mc.player.getPos();
        Vec3d nextPos = pos.add(vel.x * 2, 0, vel.z * 2);

        Box playerBox = mc.player.getBoundingBox();
        Box predictedBox = playerBox.offset(vel.x * 2, 0, vel.z * 2);

        boolean collisionX = mc.world.getBlockCollisions(mc.player, playerBox.offset(vel.x * 2, 0, 0)).iterator().hasNext();
        boolean collisionZ = mc.world.getBlockCollisions(mc.player, playerBox.offset(0, 0, vel.z * 2)).iterator().hasNext();

        if (autoStrafe.isEnabled()) {
            if (collisionX && !collisionZ) {
                mc.player.setVelocity(0, vel.y, vel.z);
            } else if (collisionZ && !collisionX) {
                mc.player.setVelocity(vel.x, vel.y, 0);
            } else if (collisionX && collisionZ) {
                float yaw = mc.player.getYaw();
                double yawRad = Math.toRadians(yaw + 90);
                double strafeSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
                mc.player.setVelocity(Math.cos(yawRad) * strafeSpeed, vel.y, Math.sin(yawRad) * strafeSpeed);
            }
        }
    }
}
