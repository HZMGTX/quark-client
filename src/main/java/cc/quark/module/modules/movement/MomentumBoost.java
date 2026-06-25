package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * MomentumBoost - retains horizontal momentum when the player changes direction,
 * preventing the sharp deceleration that normally occurs when strafing or
 * turning during a sprint.
 *
 * <p>Each tick the previous horizontal velocity is blended with the new
 * (post-physics) velocity using the {@code Retention} factor.  A retention of
 * 0.85 means 85 % of the old speed carries over each tick, giving a
 * gradual direction change rather than an abrupt stop.
 */
public class MomentumBoost extends Module {

    private final DoubleSetting retention = register(new DoubleSetting(
            "Retention", "Fraction of momentum kept per tick (0 = none, 1 = full)", 0.85, 0.0, 0.99));

    private double prevVx = 0;
    private double prevVz = 0;

    public MomentumBoost() {
        super("MomentumBoost", "Keep momentum when changing direction", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        prevVx = 0;
        prevVz = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround() && mc.player.getVelocity().y < -0.1) {
            // Don't interfere while falling
            return;
        }

        Vec3d vel = mc.player.getVelocity();
        double r = retention.get();

        // Blend current velocity with previous tick's velocity
        double blendX = vel.x + (prevVx - vel.x) * r;
        double blendZ = vel.z + (prevVz - vel.z) * r;

        // Only boost if the blended speed is actually higher than current
        double curH   = Math.sqrt(vel.x   * vel.x   + vel.z   * vel.z);
        double blendH = Math.sqrt(blendX  * blendX  + blendZ  * blendZ);

        if (blendH > curH && curH > 0.01) {
            mc.player.setVelocity(blendX, vel.y, blendZ);
        }

        prevVx = mc.player.getVelocity().x;
        prevVz = mc.player.getVelocity().z;
    }
}
