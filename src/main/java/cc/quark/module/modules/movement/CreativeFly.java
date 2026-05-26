package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

/**
 * CreativeFly - mimics creative-mode style flight, smoothly hovering and moving
 * with the directional keys while zeroing fall distance.
 */
public class CreativeFly extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Flight speed", 0.6, 0.1, 3.0));

    public CreativeFly() {
        super("CreativeFly", "Creative-style flight", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        double s = speed.get();
        double y = 0;
        if (mc.options.jumpKey.isPressed()) y += s;
        if (mc.options.sneakKey.isPressed()) y -= s;
        float fwd = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        double yawRad = Math.toRadians(mc.player.getYaw());
        double x = (-Math.sin(yawRad) * fwd + Math.cos(yawRad) * side) * s;
        double z = (Math.cos(yawRad) * fwd + Math.sin(yawRad) * side) * s;
        mc.player.setVelocity(x, y, z);
        mc.player.fallDistance = 0;
    }
}
