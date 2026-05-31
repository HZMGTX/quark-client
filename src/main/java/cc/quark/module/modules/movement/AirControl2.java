package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * AirControl2 - enhanced mid-air directional control with optional strafing boost.
 *
 * <p>In vanilla Minecraft, directional control while airborne is heavily
 * dampened. This module increases air strafing responsiveness and optionally
 * applies a boost to sideways (strafe) movement while in the air.
 *
 * <ul>
 *   <li><b>Air Multiplier</b> - scales all horizontal air velocity.</li>
 *   <li><b>Strafe Boost</b>   - additional multiplier for sideways input.</li>
 *   <li><b>No Drag</b>        - prevents vanilla air drag from reducing speed.</li>
 * </ul>
 */
public class AirControl2 extends Module {

    private final DoubleSetting airMultiplier = register(new DoubleSetting(
            "Air Multiplier", "Horizontal velocity multiplier while airborne", 1.4, 1.0, 3.0));

    private final DoubleSetting strafeBoost = register(new DoubleSetting(
            "Strafe Boost", "Extra multiplier applied to sideways (strafe) input", 1.6, 1.0, 4.0));

    private final BoolSetting noDrag = register(new BoolSetting(
            "No Drag", "Prevent air drag from reducing horizontal speed", false));

    private final BoolSetting onlyWhenMoving = register(new BoolSetting(
            "Only When Moving", "Only apply when player is pressing movement keys", true));

    public AirControl2() {
        super("AirControl2", "Enhanced air control with strafing boost while airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;

        if (onlyWhenMoving.isEnabled()) {
            boolean moving = mc.player.input.movementForward != 0
                          || mc.player.input.movementSideways != 0;
            if (!moving) return;
        }

        Vec3d vel = mc.player.getVelocity();
        float yaw = (float) Math.toRadians(mc.player.getYaw());

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) return;

        double len = Math.sqrt(fwd * fwd + side * side);
        double nFwd  = fwd  / len;
        double nSide = side / len;

        // Decompose into world-space direction
        double wx = (-Math.sin(yaw) * nFwd + Math.cos(yaw) * nSide);
        double wz = ( Math.cos(yaw) * nFwd + Math.sin(yaw) * nSide);

        // Apply air multiplier
        double mult = airMultiplier.get();

        // Extra boost for strafe-only input
        if (Math.abs(side) > Math.abs(fwd)) {
            mult *= strafeBoost.get();
        }

        double newX = vel.x + wx * (mult - 1.0) * 0.05;
        double newZ = vel.z + wz * (mult - 1.0) * 0.05;

        if (noDrag.isEnabled()) {
            // Keep the current speed, counteract vanilla air drag (factor ~0.98 per tick)
            newX /= 0.98;
            newZ /= 0.98;
        }

        mc.player.setVelocity(newX, vel.y, newZ);
    }
}
