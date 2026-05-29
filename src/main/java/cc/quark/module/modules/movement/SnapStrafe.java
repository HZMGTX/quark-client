package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * SnapStrafe - snap movement direction to the nearest 45° angle and apply a
 * speed multiplier through EventMove. Normalizes X/Z to 45° multiples.
 */
public class SnapStrafe extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Movement speed multiplier", 1.0, 0.5, 3.0));

    public SnapStrafe() {
        super("SnapStrafe", "Snap movement to nearest 45° and apply speed multiplier", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        // Compute the movement yaw offset from player yaw
        double moveAngle = Math.atan2(-fwd, side); // raw input angle
        // Snap to nearest 45° multiple (0, 45, 90, 135, 180, -45, -90, -135)
        double snapped = Math.round(moveAngle / (Math.PI / 4.0)) * (Math.PI / 4.0);

        double yawRad  = Math.toRadians(mc.player.getYaw());
        double absAngle = yawRad + snapped;

        double s = 0.26 * speed.get();
        double nx = -Math.sin(absAngle) * s;
        double nz =  Math.cos(absAngle) * s;

        event.setX(nx);
        event.setZ(nz);
    }
}
