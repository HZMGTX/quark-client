package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * WallJump - jump off walls by pressing jump while touching a wall (horizontal
 * collision).  Launches the player away from the wall with configurable force.
 */
public class WallJump extends Module {

    private final DoubleSetting upForce = register(new DoubleSetting(
            "Up Force", "Upward velocity applied on wall jump", 0.5, 0.1, 1.5));
    private final DoubleSetting awayForce = register(new DoubleSetting(
            "Away Force", "Horizontal velocity away from the wall", 0.4, 0.1, 1.0));

    private boolean wasJumping = false;

    public WallJump() {
        super("WallJump", "Jump off walls by pressing jump while touching one", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasJumping = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean jumpPressed = mc.player.input.jumping;
        boolean touchingWall = mc.player.horizontalCollision;
        boolean inAir = !mc.player.isOnGround();

        // Detect fresh jump key press (edge trigger) while touching a wall in the air
        if (jumpPressed && !wasJumping && touchingWall && inAir) {
            // Calculate away-from-wall direction (opposite of the collision normal,
            // approximated by reversing current velocity direction)
            Vec3d vel = mc.player.getVelocity();
            double hLen = Math.sqrt(vel.x * vel.x + vel.z * vel.z);

            double awayX, awayZ;
            if (hLen > 0.01) {
                // Bounce back
                awayX = -(vel.x / hLen) * awayForce.get();
                awayZ = -(vel.z / hLen) * awayForce.get();
            } else {
                // Fall back to look direction if no horizontal velocity
                float yaw = (float) Math.toRadians(mc.player.getYaw());
                awayX = Math.sin(yaw) * awayForce.get();
                awayZ = -Math.cos(yaw) * awayForce.get();
            }

            mc.player.setVelocity(awayX, upForce.get(), awayZ);
            mc.player.fallDistance = 0;
        }

        wasJumping = jumpPressed;
    }
}
