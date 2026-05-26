package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.util.math.Vec3d;

/**
 * Vanilla - vanilla-style speed that sets horizontal velocity directly from the
 * player's input direction each ground tick.
 */
public class Vanilla extends Module {

    public Vanilla() {
        super("Vanilla", "Vanilla-safe speed boost", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;
        mc.player.setSprinting(true);
        double yawRad = Math.toRadians(mc.player.getYaw());
        double len = Math.sqrt(fwd * fwd + side * side);
        double s = 0.275;
        double x = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) / len * s;
        double z = (Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) / len * s;
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(x, v.y, z);
    }
}
