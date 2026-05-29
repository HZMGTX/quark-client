package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Vanilla - restore vanilla-style movement by zeroing client velocity
 * modifications each tick and recomputing from input.
 * Mode: Ground (only on ground), Air (only in air), Both.
 */
public class Vanilla extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "When to apply vanilla movement restoration",
            "Ground", "Ground", "Air", "Both"));

    public Vanilla() {
        super("Vanilla", "Restore vanilla movement; zero external velocity modifications", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        String m = mode.get();

        boolean apply = m.equals("Both")
                || (m.equals("Ground") && onGround)
                || (m.equals("Air")    && !onGround);

        if (!apply) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        // Recompute vanilla-style velocity from input + yaw
        double yawRad = Math.toRadians(mc.player.getYaw());
        double len = Math.sqrt(fwd * fwd + side * side);
        double spd = onGround ? 0.215 : 0.1;

        double x = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) / len * spd;
        double z = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) / len * spd;

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(x, v.y, z);
    }
}
