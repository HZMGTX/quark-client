package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Bhop2 - auto bunny-hop that jumps on landing and maintains horizontal speed.
 */
public class Bhop2 extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Horizontal speed multiplier", 1.2, 1.0, 2.0));

    public Bhop2() {
        super("Bhop2", "Automatic bunny hop", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;
        if (!isMoving()) return;
        if (mc.player.isOnGround()) {
            mc.player.jump();
        } else {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x * boost.get(), v.y, v.z * boost.get());
        }
    }

    private boolean isMoving() {
        return mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
    }
}
