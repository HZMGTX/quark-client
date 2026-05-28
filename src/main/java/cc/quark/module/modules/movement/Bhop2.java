package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

public class Bhop2 extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Bhop mode", "Normal", "Normal", "Ground"));

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal speed multiplier", 1.0, 1.0, 3.0));

    public Bhop2() {
        super("Bhop2", "Automatic bunny hop", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!isMoving()) return;

        boolean onGround = mc.player.isOnGround();

        if (mode.is("Ground")) {
            if (onGround) {
                Vec3d vel = mc.player.getVelocity();
                double[] dir = getMovementDirection();
                double boost = 0.215 * speed.get();
                mc.player.setVelocity(dir[0] * boost, 0.42, dir[1] * boost);
            }
            return;
        }

        if (onGround) {
            Vec3d vel = mc.player.getVelocity();
            double[] dir = getMovementDirection();
            double boost = 0.215 * speed.get();
            mc.player.setVelocity(dir[0] * boost, 0.42, dir[1] * boost);
        } else {
            Vec3d vel = mc.player.getVelocity();
            double hLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            if (hLen > 0) {
                double boosted = hLen * (0.99 + (speed.get() - 1.0) * 0.01);
                double scale = boosted / hLen;
                mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
            }
        }
    }

    private boolean isMoving() {
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }

    private double[] getMovementDirection() {
        float yaw = mc.player.getYaw();
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(yaw);
        if (fwd == 0 && side == 0) {
            return new double[]{ -Math.sin(yawRad), Math.cos(yawRad) };
        }
        double len = Math.sqrt(fwd * fwd + side * side);
        double nFwd = fwd / len;
        double nSide = side / len;
        double x = -Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide;
        double z = Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide;
        return new double[]{ x, z };
    }
}
