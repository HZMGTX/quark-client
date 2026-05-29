package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.util.math.Vec3d;

/**
 * Dolphin - dolphin-style swimming: periodic upward surges in water combined
 * with forward bursts to mimic natural dolphin movement.  Works best with the
 * jump key held.
 *
 * <p>The module fires a velocity burst every {@code (1000 / Speed)} ms, pushing
 * the player forward and slightly upward.  On reaching the surface it applies a
 * brief additional upward kick to breach above the water line.
 */
public class Dolphin extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Forward burst speed per surge", 1.2, 0.2, 5.0));
    private final DoubleSetting upwardKick = register(new DoubleSetting(
            "Upward Kick", "Vertical component of each surge", 0.35, 0.0, 1.0));
    private final DoubleSetting interval = register(new DoubleSetting(
            "Interval", "Milliseconds between surges", 500, 100, 2000));
    private final BoolSetting requireJump = register(new BoolSetting(
            "Require Jump", "Only surge when jump key is held", true));

    private final TimerUtil timer = new TimerUtil();

    public Dolphin() {
        super("Dolphin", "Periodic dolphin-style surge in water", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;
        if (requireJump.isEnabled() && !mc.options.jumpKey.isPressed()) return;

        if (!timer.hasReached(interval.get())) return;
        timer.reset();

        float yaw   = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);
        float fwd   = mc.player.input.movementForward;
        float side  = mc.player.input.movementSideways;

        double spd = speed.get() * 0.2;
        double dx  = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd;
        double dz  = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd;
        // If no input use look direction
        if (fwd == 0 && side == 0) {
            dx = -Math.sin(yawRad) * spd;
            dz =  Math.cos(yawRad) * spd;
        }

        Vec3d vel = mc.player.getVelocity();

        // Extra kick if near water surface (about to breach)
        boolean nearSurface = !mc.player.isSubmergedInWater();
        double vy = upwardKick.get() * (nearSurface ? 1.5 : 1.0);

        mc.player.setVelocity(
                vel.x + dx,
                vel.y + vy,
                vel.z + dz);
    }
}
