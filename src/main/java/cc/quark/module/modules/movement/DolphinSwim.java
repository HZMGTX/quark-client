package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

/**
 * DolphinSwim - Amplifies forward velocity when in water with Dolphin's Grace effect.
 * Distinct from FastSwim which provides a flat speed boost in any liquid.
 */
public class DolphinSwim extends Module {

    private final DoubleSetting speedMult = register(new DoubleSetting("SpeedMult", "Velocity multiplier while swimming with Dolphin's Grace", 1.5, 1.0, 3.0));

    public DolphinSwim() {
        super("DolphinSwim", "Amplifies swimming speed when Dolphin's Grace effect is active", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;
        if (!mc.player.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double mult = speedMult.get();

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side);
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side);

        Vec3d vel = mc.player.getVelocity();

        // Amplify the horizontal component
        double newX = vel.x + dx * (mult - 1.0) * 0.05;
        double newZ = vel.z + dz * (mult - 1.0) * 0.05;

        mc.player.setVelocity(newX, vel.y, newZ);
    }
}
