package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * Slide - maintain momentum on the ground after input stops.
 * FrictionFactor (0.5-0.99) controls how much velocity is retained per tick,
 * producing a smooth deceleration rather than an instant stop.
 */
public class Slide extends Module {

    private final DoubleSetting friction = register(new DoubleSetting(
            "Friction Factor", "Fraction of velocity retained per tick (higher = more slide)", 0.92, 0.5, 0.99));

    public Slide() {
        super("Slide", "Preserve ground momentum after releasing movement keys", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;

        boolean moving = mc.player.input.movementForward != 0
                      || mc.player.input.movementSideways != 0;
        if (moving) return;

        Vec3d v = mc.player.getVelocity();
        double hLen = Math.sqrt(v.x * v.x + v.z * v.z);
        if (hLen < 0.002) {
            mc.player.setVelocity(0.0, v.y, 0.0);
            return;
        }

        double factor = friction.get();
        mc.player.setVelocity(v.x * factor, v.y, v.z * factor);
    }
}
