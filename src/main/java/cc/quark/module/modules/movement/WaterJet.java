package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * WaterJet - applies strong forward thrust while the player is in water and a
 * movement key is pressed. Distinct from WaterSpeed which provides a gentle
 * ongoing boost.
 */
public class WaterJet extends Module {

    private final DoubleSetting thrust = register(new DoubleSetting(
            "Thrust", "Forward thrust applied per tick while in water", 0.3, 0.1, 1.0));

    public WaterJet() {
        super("WaterJet", "Strong forward thrust in water when movement keys are pressed", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isTouchingWater()) return;

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) return;

        double yawRad = Math.toRadians(mc.player.getYaw());
        double pitchRad = Math.toRadians(mc.player.getPitch());

        // Compute thrust direction considering pitch for vertical movement
        double len = Math.sqrt(fwd * fwd + side * side);
        double normFwd  = fwd  / len;
        double normSide = side / len;

        double tx = (-Math.sin(yawRad) * normFwd + Math.cos(yawRad) * normSide) * thrust.get();
        double tz = ( Math.cos(yawRad) * normFwd + Math.sin(yawRad) * normSide) * thrust.get();
        // Include vertical component when looking up/down
        double ty = -Math.sin(pitchRad) * normFwd * thrust.get();

        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x + tx, vel.y + ty, vel.z + tz);
    }
}
