package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * WaterSpeed2 - improves the player's movement speed inside water by
 * supplementing vanilla water physics with direct velocity boosts.
 *
 * <p>Vanilla water caps horizontal movement significantly.  This module
 * re-applies a configurable speed boost in the player's movement direction
 * every tick they are submerged or touching water.
 */
public class WaterSpeed2 extends Module {

    private final DoubleSetting speedMultiplier = register(new DoubleSetting(
            "Multiplier", "Speed multiplier inside water (1.0 = vanilla)", 2.5, 1.0, 8.0));

    private final DoubleSetting verticalBoost = register(new DoubleSetting(
            "Vertical Boost", "Upward velocity applied when holding jump in water", 0.15, 0.0, 0.5));

    private final BoolSetting boostSurface = register(new BoolSetting(
            "Boost Surface", "Also apply boost when swimming on the water surface", true));

    private final BoolSetting requireMoving = register(new BoolSetting(
            "Require Moving", "Only apply boost when movement keys are pressed", true));

    public WaterSpeed2() {
        super("WaterSpeed2", "Improves water movement speed and swim responsiveness", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean inWater = mc.player.isTouchingWater();
        if (!inWater) return;

        boolean fullySubmerged = mc.player.isSubmergedIn(FluidTags.WATER);
        if (!fullySubmerged && !boostSurface.isEnabled()) return;

        if (requireMoving.isEnabled()) {
            boolean moving = mc.player.input.movementForward != 0
                          || mc.player.input.movementSideways != 0;
            if (!moving) return;
        }

        float fwd  = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        if (fwd == 0 && side == 0) return;

        float yaw = (float) Math.toRadians(mc.player.getYaw());
        double len = Math.sqrt(fwd * fwd + side * side);
        double nFwd  = fwd  / len;
        double nSide = side / len;

        double wx = (-Math.sin(yaw) * nFwd + Math.cos(yaw) * nSide);
        double wz = ( Math.cos(yaw) * nFwd + Math.sin(yaw) * nSide);

        Vec3d vel = mc.player.getVelocity();

        // Water speed is typically around 0.05-0.07 blocks/tick
        // Apply multiplied speed in movement direction
        double targetSpeed = 0.06 * speedMultiplier.get();
        double newX = wx * targetSpeed;
        double newZ = wz * targetSpeed;

        double newY = vel.y;
        if (fullySubmerged && mc.options.jumpKey.isPressed() && verticalBoost.get() > 0) {
            newY = verticalBoost.get();
        }

        mc.player.setVelocity(newX, newY, newZ);
    }
}
