package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * ForwardBoost - applies a one-time velocity burst in the look direction when
 * the sprint key is first pressed (leading edge detection).
 */
public class ForwardBoost extends Module {

    private final DoubleSetting boost = register(new DoubleSetting(
            "Boost", "Velocity burst applied when sprint is first pressed (blocks/tick)", 0.6, 0.1, 3.0));

    private boolean wasSprintPressed = false;

    public ForwardBoost() {
        super("ForwardBoost", "One-time speed burst when sprint key is first pressed", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasSprintPressed = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean sprintPressed = mc.options.sprintKey.isPressed();

        // Detect leading edge (key just pressed this tick)
        if (sprintPressed && !wasSprintPressed) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            double bx = -Math.sin(yaw) * boost.get();
            double bz =  Math.cos(yaw) * boost.get();

            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(bx, vel.y, bz);
            mc.player.setSprinting(true);
        }

        wasSprintPressed = sprintPressed;
    }
}
