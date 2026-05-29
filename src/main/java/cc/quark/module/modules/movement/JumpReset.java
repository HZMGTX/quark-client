package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * JumpReset - on EventAttack, release forward key for 1 tick then re-press
 * (W-tap), which resets sprint and allows re-sprinting immediately.
 * The forward key is simulated by briefly zeroing horizontal velocity on attack.
 */
public class JumpReset extends Module {

    private int releaseTimer = 0;

    public JumpReset() {
        super("JumpReset", "W-tap on attack to reset sprint for full knockback", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        releaseTimer = 0;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (!mc.player.isSprinting()) return;
        // Start the 1-tick release
        mc.player.setSprinting(false);
        releaseTimer = 1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (releaseTimer <= 0) return;

        releaseTimer--;

        if (releaseTimer == 0) {
            // Re-apply sprint and small forward velocity burst
            mc.player.setSprinting(true);
            Vec3d v = mc.player.getVelocity();
            double yawRad = Math.toRadians(mc.player.getYaw());
            double boost = 0.1;
            mc.player.setVelocity(
                    v.x - Math.sin(yawRad) * boost,
                    v.y,
                    v.z + Math.cos(yawRad) * boost);
        }
    }
}
