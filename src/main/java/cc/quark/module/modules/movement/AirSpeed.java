package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * AirSpeed - increases horizontal movement speed while the player is airborne.
 * Works by scaling the existing horizontal velocity up to the configured
 * multiplier each tick, which feels natural and stacks with jump momentum.
 */
public class AirSpeed extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Horizontal speed multiplier while airborne", 1.5, 1.0, 4.0));

    private final DoubleSetting maxSpeed = register(new DoubleSetting(
            "Max Speed", "Maximum horizontal velocity (blocks/tick)", 0.6, 0.1, 2.0));

    private final BoolSetting requireMoving = register(new BoolSetting(
            "Require Moving", "Only boost when movement keys are held", true));

    public AirSpeed() {
        super("AirSpeed", "Increases horizontal speed while airborne", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (mc.player.isFallFlying()) return; // leave elytra to ElytraBoost

        if (requireMoving.isEnabled()) {
            boolean moving = mc.player.input.movementForward != 0
                    || mc.player.input.movementSideways != 0;
            if (!moving) return;
        }

        Vec3d vel = mc.player.getVelocity();
        double hx = vel.x * multiplier.get();
        double hz = vel.z * multiplier.get();

        // Clamp to max speed
        double hSpeed = Math.sqrt(hx * hx + hz * hz);
        double cap = maxSpeed.get();
        if (hSpeed > cap) {
            double scale = cap / hSpeed;
            hx *= scale;
            hz *= scale;
        }

        mc.player.setVelocity(hx, vel.y, hz);
    }
}
