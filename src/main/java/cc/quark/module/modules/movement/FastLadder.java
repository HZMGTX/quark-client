package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * FastLadder - dramatically increases ladder and vine climbing speed.
 *
 * <p>Vanilla ladder ascent is capped at roughly 0.12 blocks/tick.  This module
 * replaces that with a configurable value.  Forward key climbs up, sneak
 * descends; horizontal movement is untouched so left/right steering still works.
 *
 * <p>Unlike {@link FastClimb}, this module does not need any jump-key logic —
 * just forward = up, sneak = down.  Disabling the slowdown effect lets full
 * normal horizontal speed carry over.
 */
public class FastLadder extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Vertical climb speed on ladders/vines (blocks/tick)", 0.5, 0.05, 2.0));
    private final BoolSetting noFallDamage = register(new BoolSetting(
            "No Fall Damage", "Reset fall distance while on a climbable block", true));
    private final BoolSetting preserveHorizontal = register(new BoolSetting(
            "Preserve Horizontal", "Keep horizontal velocity while climbing", true));

    public FastLadder() {
        super("FastLadder", "High-speed ladder and vine climbing", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isClimbing()) return;

        Vec3d v = mc.player.getVelocity();

        // Determine vertical direction from sneak key (descend) or default (ascend)
        double vy = mc.options.sneakKey.isPressed() ? -speed.get() : speed.get();

        // Also use forward input: if the player is pressing back, descend
        if (mc.player.input.movementForward < 0) {
            vy = -speed.get();
        }

        double vx = preserveHorizontal.isEnabled() ? v.x : 0;
        double vz = preserveHorizontal.isEnabled() ? v.z : 0;

        mc.player.setVelocity(vx, vy, vz);

        if (noFallDamage.isEnabled()) {
            mc.player.fallDistance = 0;
        }
    }
}
