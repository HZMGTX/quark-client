package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * AirControl - while airborne, apply WASD directional steering force to let the
 * player redirect their horizontal (and optionally vertical) trajectory mid-air.
 */
public class AirControl extends Module {

    private final BoolSetting horizontalControl = register(new BoolSetting(
            "Horizontal", "Allow horizontal direction change while airborne", true));
    private final BoolSetting verticalControl = register(new BoolSetting(
            "Vertical", "Allow jump/sneak to adjust vertical velocity while airborne", false));
    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Strength of air steering force applied per tick", 0.08, 0.01, 0.5));

    public AirControl() {
        super("AirControl", "Full WASD directional control while airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double mult = multiplier.get();

        if (horizontalControl.isEnabled() && (fwd != 0 || side != 0)) {
            double yawRad = Math.toRadians(mc.player.getYaw());
            // Project WASD onto world axes relative to look direction
            double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * mult;
            double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * mult;
            event.setX(event.getX() + dx);
            event.setZ(event.getZ() + dz);
        }

        if (verticalControl.isEnabled()) {
            double dy = 0;
            if (mc.player.input.jumping)  dy += mult * 0.5;
            if (mc.player.input.sneaking) dy -= mult * 0.5;
            if (dy != 0) event.setY(event.getY() + dy);
        }
    }
}
