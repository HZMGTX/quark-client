package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * AntiWater - cancels the speed reduction that water applies to the player,
 * allowing movement at (approximately) normal walking/sprinting speed while
 * submerged.
 *
 * <p>Vanilla water applies a drag factor of roughly 0.8 per tick.  This module
 * directly sets horizontal velocity based on input each tick, bypassing that
 * drag.
 */
public class AntiWater extends Module {

    private final DoubleSetting speedMult = register(new DoubleSetting(
            "Speed", "Horizontal speed in water (blocks/tick)", 1.0, 0.1, 5.0));

    public AntiWater() {
        super("AntiWater", "Move at normal speed in water", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double spd    = speedMult.get() * 0.215; // 0.215 ~ vanilla sprint b/t

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side);
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side);
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0) { dx = dx / len * spd; dz = dz / len * spd; }

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(dx, vel.y, dz);
    }
}
