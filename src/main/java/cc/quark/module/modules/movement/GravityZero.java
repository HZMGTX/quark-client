package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * GravityZero - zeroes the Y component of every move event while airborne,
 * effectively removing gravity so the player drifts at their current height.
 *
 * <p>This is a simpler, always-zero-G version of {@link GravityControl}.
 * When {@code Allow Upward} is true, positive Y values (initial jump arc)
 * are preserved so jumping still works.
 */
public class GravityZero extends Module {

    private final BoolSetting allowUpward = register(new BoolSetting(
            "Allow Upward", "Keep upward velocity from jumps/launches", true));

    public GravityZero() {
        super("GravityZero", "Remove gravity — hover at current altitude", Category.MOVEMENT);
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround() || mc.player.isTouchingWater()) return;

        double y = event.getY();

        if (allowUpward.isEnabled()) {
            // Only cancel downward component
            if (y < 0) event.setY(0.0);
        } else {
            event.setY(0.0);
        }

        mc.player.fallDistance = 0;
    }
}
