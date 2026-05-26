package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * GravityZero - removes downward acceleration so the player drifts in place.
 */
public class GravityZero extends Module {

    public GravityZero() {
        super("GravityZero", "Zero vertical gravity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround() || mc.player.isTouchingWater()) return;
        Vec3d v = mc.player.getVelocity();
        if (v.y < 0) {
            mc.player.setVelocity(v.x, 0.0, v.z);
        }
    }
}
