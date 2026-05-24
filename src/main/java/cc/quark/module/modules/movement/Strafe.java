package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * Strafe â€” adjusts the player's horizontal velocity to match the look direction,
 * providing smoother strafing motion for combat and movement modules.
 */
public class Strafe extends Module {

    private final DoubleSetting speed;

    public Strafe() {
        super("Strafe", "Improves strafing motion while moving.", Category.MOVEMENT);
        speed = doubleSetting("Speed", "Strafing speed multiplier.", 1.0, 0.1, 3.0);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;

        float yaw     = mc.player.getYaw();
        float yawRad  = (float) Math.toRadians(yaw);

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) return;

        double len = Math.sqrt(fwd * fwd + side * side);
        if (len == 0) return;
        fwd  = (float) (fwd  / len);
        side = (float) (side / len);

        double s = speed.get() * 0.26;  // baseline walk speed ~0.26 b/t
        double x = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * s;
        double z = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * s;

        event.setX(x);
        event.setZ(z);
    }
}
