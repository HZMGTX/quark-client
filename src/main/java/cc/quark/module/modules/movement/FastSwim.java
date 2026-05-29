package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

/**
 * FastSwim - makes the player move faster through water (and optionally lava).
 *
 * <p>Combines:
 * <ul>
 *   <li>Direct velocity override in the input direction.</li>
 *   <li>Dolphin's Grace status effect for smoother server-side motion.</li>
 *   <li>Optional upward boost when the jump key is held.</li>
 * </ul>
 */
public class FastSwim extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Swim speed (blocks/tick)", 0.35, 0.05, 2.0));
    private final BoolSetting upBoost = register(new BoolSetting(
            "Up Boost", "Boost upward speed when jump key is held", true));
    private final BoolSetting dolphinsGrace = register(new BoolSetting(
            "Dolphins Grace", "Apply Dolphins Grace effect for smooth swimming", true));
    private final BoolSetting inLava = register(new BoolSetting(
            "In Lava", "Also apply fast movement in lava", false));

    public FastSwim() {
        super("FastSwim", "High-speed water (and lava) movement", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean inWater   = mc.player.isTouchingWater();
        boolean inLavaFluid = mc.player.isInLava();

        boolean active = inWater || (inLava.isEnabled() && inLavaFluid);
        if (!active) return;

        // Apply Dolphin's Grace so server-side movement is smooth
        if (dolphinsGrace.isEnabled() && inWater) {
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.DOLPHINS_GRACE, 20, 0, false, false));
        }

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        boolean jumpHeld = mc.options.jumpKey.isPressed();

        if (fwd == 0 && side == 0 && !jumpHeld) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double spd    = speed.get();

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * spd;
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * spd;

        Vec3d vel = mc.player.getVelocity();
        double dy = vel.y;

        if (upBoost.isEnabled() && jumpHeld) {
            dy = Math.max(dy, spd * 0.8);
        } else if (mc.options.sneakKey.isPressed()) {
            dy = -spd * 0.5;
        }

        if (fwd != 0 || side != 0) {
            mc.player.setVelocity(dx, dy, dz);
        } else {
            mc.player.setVelocity(vel.x, dy, vel.z);
        }
    }
}
