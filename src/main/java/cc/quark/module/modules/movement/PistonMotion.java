package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * PistonMotion - detects sudden upward velocity spikes (such as those caused by
 * piston launches) and amplifies them to ride piston-based launchers much higher.
 */
public class PistonMotion extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Multiplier applied to detected piston launch velocity", 2.0, 1.0, 10.0));

    /** Threshold above which an upward velocity spike is treated as a piston launch. */
    private static final double PISTON_LAUNCH_THRESHOLD = 0.4;

    private double prevVelY = 0.0;

    public PistonMotion() {
        super("PistonMotion", "Rides piston launches for high velocity", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        prevVelY = 0.0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        Vec3d vel = mc.player.getVelocity();
        double velY = vel.y;

        // Detect sudden upward velocity spike not caused by jumping
        boolean notJumping = !mc.player.input.jumping;
        boolean spiked = velY > PISTON_LAUNCH_THRESHOLD && prevVelY <= 0.0;

        if (spiked && notJumping) {
            mc.player.setVelocity(vel.x, velY * boost.get(), vel.z);
            mc.player.fallDistance = 0;
        }

        prevVelY = velY;
    }
}
