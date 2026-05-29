package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * MoonWalk - reverse horizontal momentum each tick for a glide/slide effect.
 * Speed setting controls how quickly the reversal is applied. While falling,
 * low gravity reduces Y deceleration for floaty arcs.
 */
public class MoonWalk extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Reverse momentum strength", 0.2, 0.05, 1.0));
    private final DoubleSetting gravity = register(new DoubleSetting(
            "Gravity", "Gravity factor (lower = floatier arc)", 0.3, 0.05, 1.0));

    public MoonWalk() {
        super("MoonWalk", "Reverse horizontal momentum for slide/glide; low gravity arcs", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;

        Vec3d vel = mc.player.getVelocity();

        // Reverse horizontal momentum (the "moonwalk" slide)
        double hLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hLen > 0.001) {
            double s = speed.get();
            double nx = vel.x - (vel.x / hLen) * s * 2.0;
            double nz = vel.z - (vel.z / hLen) * s * 2.0;
            // Clamp so we don't overshoot into backwards movement
            double newLen = Math.sqrt(nx * nx + nz * nz);
            if (newLen > hLen * 1.5) {
                nx = (nx / newLen) * hLen;
                nz = (nz / newLen) * hLen;
            }
            mc.player.setVelocity(nx, vel.y, nz);
        }

        // Low-gravity fall
        if (!mc.player.isOnGround() && vel.y < 0) {
            Vec3d updated = mc.player.getVelocity();
            mc.player.setVelocity(updated.x, updated.y * gravity.get(), updated.z);
        }
    }
}
