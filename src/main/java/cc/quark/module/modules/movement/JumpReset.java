package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * JumpReset - nudges the player upward when hurt mid-air to reset fall speed.
 */
public class JumpReset extends Module {

    public JumpReset() {
        super("JumpReset", "Resets vertical knockback", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.hurtTime <= 0 || mc.player.isOnGround()) return;
        Vec3d v = mc.player.getVelocity();
        if (v.y < 0) {
            mc.player.setVelocity(v.x, v.y * 0.5, v.z);
        }
    }
}
