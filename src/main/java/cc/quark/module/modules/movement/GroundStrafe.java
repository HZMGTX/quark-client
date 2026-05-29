package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * GroundStrafe - while on the ground, align horizontal velocity precisely with
 * the optimal 45° strafe angle to maximise speed when strafing diagonally.
 *
 * <p>The player's WASD input is projected onto the look-direction and a clean
 * velocity vector is applied at the target speed, eliminating the slight speed
 * penalty from imprecise diagonal input.
 */
public class GroundStrafe extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Target horizontal speed on ground (blocks/tick)", 0.28, 0.05, 1.0));
    private final BoolSetting requireSprint = register(new BoolSetting(
            "Require Sprint", "Only apply boost when sprinting", false));
    private final BoolSetting smoothing = register(new BoolSetting(
            "Smoothing", "Lerp toward target velocity instead of snapping", false));

    public GroundStrafe() {
        super("GroundStrafe", "Optimal 45-degree strafe alignment on the ground", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (requireSprint.isEnabled() && !mc.player.isSprinting()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double spd    = speed.get();

        // Project input onto world axes
        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side);
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side);

        // Normalize and scale
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0) { dx = dx / len * spd; dz = dz / len * spd; }

        Vec3d v = mc.player.getVelocity();

        if (smoothing.isEnabled()) {
            double alpha = 0.4; // lerp factor
            mc.player.setVelocity(v.x + (dx - v.x) * alpha, v.y, v.z + (dz - v.z) * alpha);
        } else {
            mc.player.setVelocity(dx, v.y, dz);
        }
    }
}
