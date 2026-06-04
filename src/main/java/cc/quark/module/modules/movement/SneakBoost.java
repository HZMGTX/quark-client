package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SneakBoost - applies a horizontal speed boost while the player is sneaking,
 * overriding the vanilla sneak speed penalty.
 *
 * <p>Vanilla sneak speed is approximately 0.065 blocks/tick.  This module
 * replaces that with a configurable value, making the player move at a boosted
 * pace while crouching.
 */
public class SneakBoost extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Speed while sneaking (blocks/tick)", 1.3, 0.1, 5.0));

    public SneakBoost() {
        super("SneakBoost", "Move faster while sneaking", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double spd    = boost.get() * 0.1; // scale to blocks/tick

        double dx = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side);
        double dz = ( Math.cos(yawRad) * fwd + Math.sin(yawRad) * side);
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0) { dx = dx / len * spd; dz = dz / len * spd; }

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(dx, vel.y, dz);
    }
}
