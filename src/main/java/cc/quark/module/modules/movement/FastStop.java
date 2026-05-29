package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * FastStop - decelerates the player faster than vanilla when movement keys are
 * released.  Vanilla friction is around 0.91 on air; this module multiplies
 * horizontal velocity by a configurable lower factor each move event when no
 * directional keys are held.
 */
public class FastStop extends Module {

    private final DoubleSetting friction = register(new DoubleSetting(
            "Friction", "Horizontal velocity multiplier per tick when stopping (lower = faster stop)",
            0.3, 0.0, 1.0));

    public FastStop() {
        super("FastStop", "Decelerate faster when releasing movement keys", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        // Only apply when no movement keys are held
        if (fwd != 0 || side != 0) return;

        double f = friction.get();
        event.setX(event.getX() * f);
        event.setZ(event.getZ() * f);

        // Also directly dampen player velocity to ensure smooth stop
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x * f, vel.y, vel.z * f);
    }
}
