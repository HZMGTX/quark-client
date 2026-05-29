package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;

/**
 * AirStrafe - while airborne, replace or boost lateral (strafe) velocity so the
 * player can steer sideways at full configured speed regardless of their current
 * momentum.
 *
 * <ul>
 *   <li><b>Normal</b>  - sets horizontal velocity to the strafe direction each tick.</li>
 *   <li><b>NoAccel</b> - only prevents horizontal acceleration build-up; caps speed.</li>
 * </ul>
 */
public class AirStrafe extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Strafe mode", "Normal", "Normal", "NoAccel"));
    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal strafe speed (blocks/tick)", 0.28, 0.05, 0.8));

    public AirStrafe() {
        super("AirStrafe", "Lateral boost while airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double spd    = speed.get();
        double yawRad = Math.toRadians(mc.player.getYaw());
        double dx     = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd;
        double dz     = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd;

        if (mode.is("Normal")) {
            // Replace horizontal velocity entirely
            event.setX(dx);
            event.setZ(dz);
        } else {
            // NoAccel: only set if current speed would exceed the cap
            double curH = Math.sqrt(event.getX() * event.getX() + event.getZ() * event.getZ());
            if (curH < spd) {
                event.setX(dx);
                event.setZ(dz);
            }
        }
    }
}
