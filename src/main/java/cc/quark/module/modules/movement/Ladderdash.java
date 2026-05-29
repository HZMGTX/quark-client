package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Ladderdash - while on a ladder and pressing forward, trigger a horizontal
 * dash off the ladder plus upward momentum for a leap effect.
 */
public class Ladderdash extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Dash speed off ladder", 0.5, 0.1, 1.5));
    private final DoubleSetting climbSpeed = register(new DoubleSetting(
            "Climb Speed", "Upward speed while on ladder with Jump held", 0.3, 0.1, 0.6));

    private boolean wasDashing = false;

    public Ladderdash() {
        super("Ladderdash", "Dash off ladders or climb fast when Jump is held", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasDashing = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isClimbing()) {
            wasDashing = false;
            return;
        }

        Vec3d v = mc.player.getVelocity();

        // Hold Jump to climb fast
        if (mc.player.input.jumping) {
            mc.player.setVelocity(v.x, climbSpeed.get(), v.z);
            return;
        }

        // Press forward to dash off ladder
        if (mc.player.input.movementForward > 0 && !wasDashing) {
            wasDashing = true;
            double yawRad = Math.toRadians(mc.player.getYaw());
            double s = speed.get();
            double dx = -Math.sin(yawRad) * s;
            double dz =  Math.cos(yawRad) * s;
            mc.player.setVelocity(dx, 0.3, dz);
        } else if (mc.player.input.movementForward <= 0) {
            wasDashing = false;
        }
    }
}
