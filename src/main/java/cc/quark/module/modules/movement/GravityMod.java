package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

/**
 * GravityMod - adjusts how strongly gravity pulls the player down, giving
 * a lighter or heavier feel without fully disabling gravity.
 *
 * <p>Modes:
 * <ul>
 *   <li>Light  - reduces downward acceleration so the player falls slower.</li>
 *   <li>Heavy  - amplifies downward acceleration so the player falls faster.</li>
 *   <li>Custom - uses the Factor setting as a direct multiplier on downward Y
 *                velocity each tick (values below 1.0 = lighter, above = heavier).</li>
 * </ul>
 */
public class GravityMod extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Gravity feel", "Light", "Light", "Heavy", "Custom"));

    private final DoubleSetting factor = register(new DoubleSetting(
            "Factor", "Y-velocity multiplier when Mode is Custom (0.5 = half gravity, 2.0 = double)", 0.5, 0.01, 4.0));

    private final BoolSetting affectJump = register(new BoolSetting(
            "Affect Jump", "Also scale upward velocity so jumps are taller/shorter", false));

    public GravityMod() {
        super("GravityMod", "Adjusts gravity strength for a lighter or heavier feel", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;

        Vec3d vel = mc.player.getVelocity();
        double vy = vel.y;

        double multiplier;
        switch (mode.get()) {
            case "Light"  -> multiplier = 0.5;
            case "Heavy"  -> multiplier = 1.8;
            default       -> multiplier = factor.get();   // "Custom"
        }

        if (vy < 0) {
            // Falling: scale downward velocity
            vy = vy * multiplier;
        } else if (vy > 0 && affectJump.isEnabled()) {
            // Rising: inverse scale so jump height matches gravity feel
            vy = vy * multiplier;
        }

        mc.player.setVelocity(vel.x, vy, vel.z);
    }

    @Override
    public void onDisable() {
        // Nothing extra to clean up — gravity returns to normal automatically
    }
}
