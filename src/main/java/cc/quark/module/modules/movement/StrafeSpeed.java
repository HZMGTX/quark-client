package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * StrafeSpeed - applies a horizontal speed boost specifically when the player
 * is strafing (moving sideways).  Useful on servers where forward-sprint speed
 * is capped but lateral movement is less restricted, or simply to gain a
 * combat positioning advantage by circling enemies faster.
 */
public class StrafeSpeed extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Speed multiplier applied when strafing", 1.4, 1.0, 3.0));

    private final DoubleSetting maxSpeed = register(new DoubleSetting(
            "Max Speed", "Horizontal velocity cap (blocks/tick)", 0.5, 0.1, 1.5));

    private final BoolSetting requireOnGround = register(new BoolSetting(
            "Require On Ground", "Only boost while on the ground", false));

    private final BoolSetting sprint = register(new BoolSetting(
            "Sprint", "Force sprint while strafing for knockback bonus", true));

    public StrafeSpeed() {
        super("StrafeSpeed", "Increases speed when moving sideways", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        float side = mc.player.input.movementSideways;
        float fwd  = mc.player.input.movementForward;

        // Only activate when strafing (sideways input present)
        if (side == 0) return;

        if (requireOnGround.isEnabled() && !mc.player.isOnGround()) return;

        // Compute strafe direction in world space
        double yawRad = Math.toRadians(mc.player.getYaw());
        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side);
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side);
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0) { dx /= len; dz /= len; }

        Vec3d vel = mc.player.getVelocity();
        double curH = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        double targetH = Math.min(curH * boost.get(), maxSpeed.get());

        mc.player.setVelocity(dx * targetH, vel.y, dz * targetH);

        if (sprint.isEnabled() && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }
    }
}
