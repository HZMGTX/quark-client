package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class BunnyHop extends Module {

    private static final double MAX_HORIZONTAL_SPEED = 0.8;

    private final DoubleSetting speedMultiplier = register(new DoubleSetting(
            "Speed Multiplier", "Horizontal velocity multiplier per hop", 1.02, 1.0, 2.0));

    private final BoolSetting autoSprint = register(new BoolSetting(
            "Auto Sprint", "Keep sprinting while hopping", true));

    private final BoolSetting autoStrafe = register(new BoolSetting(
            "Auto Strafe", "Apply strafe velocity for additional speed during hops", false));

    public BunnyHop() {
        super("BunnyHop", "Jump immediately on landing to carry momentum", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (!moving) return;

        if (autoSprint.isEnabled()) mc.player.setSprinting(true);

        if (mc.player.isOnGround()) {
            mc.player.jump();

            Vec3d vel = mc.player.getVelocity();
            double mult = speedMultiplier.get();
            double newX = vel.x * mult;
            double newZ = vel.z * mult;

            if (autoStrafe.isEnabled()) {
                float yaw   = (float) Math.toRadians(mc.player.getYaw());
                float fwd   = mc.player.input.movementForward;
                float side  = mc.player.input.movementSideways;
                double strafeX = -Math.sin(yaw) * fwd * 0.02 + Math.cos(yaw) * side * 0.02;
                double strafeZ =  Math.cos(yaw) * fwd * 0.02 + Math.sin(yaw) * side * 0.02;
                newX += strafeX;
                newZ += strafeZ;
            }

            double horizontal = Math.sqrt(newX * newX + newZ * newZ);
            if (horizontal > MAX_HORIZONTAL_SPEED) {
                double scale = MAX_HORIZONTAL_SPEED / horizontal;
                newX *= scale;
                newZ *= scale;
            }

            mc.player.setVelocity(newX, vel.y, newZ);
        }
    }
}
