package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * Strafe - optimal strafing: normalize input direction vector so diagonal
 * movement equals cardinal movement speed; apply Speed multiplier; Sprint
 * BoolSetting forces sprinting while strafing.
 */
public class Strafe extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Strafing speed multiplier", 1.0, 0.1, 3.0));
    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Force sprint while strafing", true));
    private final BoolSetting airStrafe = register(new BoolSetting(
            "Air Strafe", "Also apply while airborne", false));

    public Strafe() {
        super("Strafe", "Optimal 45-degree normalized strafing with speed multiplier", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (!airStrafe.isEnabled() && !mc.player.isOnGround()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double len = Math.sqrt(fwd * fwd + side * side);
        double normFwd  = fwd  / len;
        double normSide = side / len;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double base   = 0.26 * speed.get();

        double x = (-Math.sin(yawRad) * normFwd + Math.cos(yawRad) * normSide) * base;
        double z = ( Math.cos(yawRad) * normFwd + Math.sin(yawRad) * normSide) * base;

        event.setX(x);
        event.setZ(z);

        if (sprint.isEnabled()) {
            mc.player.setSprinting(true);
        }
    }
}
