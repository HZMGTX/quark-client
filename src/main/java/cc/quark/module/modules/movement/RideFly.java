package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;

/**
 * RideFly - allows the player to fly while riding any entity.
 *
 * Overrides the vehicle's velocity completely each tick based on player look
 * direction, providing full 3-D flight on any ridden entity (horse, pig,
 * strider, boat, etc.). The entity still stays attached to the player.
 */
public class RideFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Horizontal fly speed (blocks/tick)", 0.5, 0.05, 5.0));
    private final DoubleSetting vertSpeed = register(new DoubleSetting(
            "Vert Speed", "Vertical speed when using jump/sneak", 0.3, 0.05, 2.0));
    private final BoolSetting noFall = register(new BoolSetting(
            "No Fall", "Prevent fall damage on the vehicle", true));
    private final BoolSetting pitchFly = register(new BoolSetting(
            "Pitch Fly", "Fly in pitch direction (look up/down to ascend/descend)", false));

    public RideFly() {
        super("RideFly", "Fly while riding any entity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.hasVehicle()) return;

        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        float yawRad   = (float) Math.toRadians(mc.player.getYaw());
        float pitchRad = (float) Math.toRadians(mc.player.getPitch());
        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double vx = 0, vz = 0, vy = 0;

        if (fwd != 0 || side != 0) {
            double len = Math.sqrt(fwd * fwd + side * side);
            double nFwd  = fwd  / len;
            double nSide = side / len;

            if (pitchFly.isEnabled()) {
                vx = (-Math.sin(yawRad) * Math.cos(pitchRad) * nFwd
                       + Math.cos(yawRad) * nSide) * speed.get();
                vy =  -Math.sin(pitchRad) * nFwd * speed.get();
                vz = ( Math.cos(yawRad) * Math.cos(pitchRad) * nFwd
                       + Math.sin(yawRad) * nSide) * speed.get();
            } else {
                vx = (-Math.sin(yawRad) * nFwd + Math.cos(yawRad) * nSide) * speed.get();
                vz = ( Math.cos(yawRad) * nFwd + Math.sin(yawRad) * nSide) * speed.get();
            }
        }

        // Vertical control via jump/sneak (overrides pitch-fly vertical when used)
        if (mc.options.jumpKey.isPressed()) {
            vy = vertSpeed.get();
        } else if (mc.options.sneakKey.isPressed()) {
            vy = -vertSpeed.get();
        } else if (fwd == 0 && side == 0) {
            // Hovering: counteract gravity
            vy = 0;
        }

        vehicle.setVelocity(vx, vy, vz);

        if (noFall.isEnabled()) {
            vehicle.fallDistance = 0;
            mc.player.fallDistance = 0;
        }
    }
}
