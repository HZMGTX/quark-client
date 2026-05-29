package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;

/**
 * Acceleration - ramps speed up from 1.0x to MaxSpeed over AccelTicks ticks
 * after leaving the ground.  Resets the counter whenever the player stops
 * moving or lands.
 */
public class Acceleration extends Module {

    private final IntSetting accelTicks = register(new IntSetting(
            "Accel Ticks", "Ticks to ramp from base to max speed", 20, 5, 60));
    private final DoubleSetting maxSpeed = register(new DoubleSetting(
            "Max Speed", "Maximum speed multiplier at full ramp", 2.0, 1.1, 5.0));
    private final DoubleSetting baseSpeed = register(new DoubleSetting(
            "Base Speed", "Multiplier applied at tick 0", 1.0, 0.5, 2.0));

    /** Ticks elapsed since the player last touched the ground while moving. */
    private int airTicks = 0;
    /** Whether we were on the ground last tick. */
    private boolean wasOnGround = true;

    public Acceleration() {
        super("Acceleration", "Gradually ramp horizontal speed after leaving ground", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        airTicks = 0;
        wasOnGround = true;
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;
        boolean moving = (fwd != 0 || side != 0);

        // Reset counter on landing or when the player stops moving
        if (onGround || !moving) {
            airTicks = 0;
            wasOnGround = onGround;
            return;
        }

        // Count ticks spent airborne and moving
        if (!onGround) {
            if (airTicks < accelTicks.get()) airTicks++;
        }

        wasOnGround = onGround;

        // Lerp multiplier from baseSpeed → maxSpeed over accelTicks
        int maxT    = Math.max(1, accelTicks.get());
        double t    = Math.min(1.0, (double) airTicks / maxT);
        double mult = baseSpeed.get() + (maxSpeed.get() - baseSpeed.get()) * t;

        event.setX(event.getX() * mult);
        event.setZ(event.getZ() * mult);
    }
}
