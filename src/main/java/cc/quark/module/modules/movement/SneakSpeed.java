package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SneakSpeed - while sneaking, override the sneak speed penalty and apply
 * normal walk speed (or a custom multiplier) so the player moves at full pace
 * while still crouched.
 */
public class SneakSpeed extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Override speed while sneaking (blocks/tick)", 0.26, 0.05, 0.8));

    public SneakSpeed() {
        super("SneakSpeed", "Remove sneak speed penalty; move at normal walk speed while crouched", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isSneaking()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        if (fwd == 0 && side == 0) return;

        // Compute direction from input and override velocity
        double yawRad = Math.toRadians(mc.player.getYaw());
        double len = Math.sqrt(fwd * fwd + side * side);
        double normFwd  = fwd  / len;
        double normSide = side / len;

        double s  = speed.get();
        double nx = (-Math.sin(yawRad) * normFwd + Math.cos(yawRad) * normSide) * s;
        double nz = ( Math.cos(yawRad) * normFwd + Math.sin(yawRad) * normSide) * s;

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(nx, v.y, nz);
    }
}
