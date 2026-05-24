package com.ghostclient.module.modules.movement;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventSlowdown;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

/**
 * NoSlowdown - cancels or reduces the various movement penalties the game applies
 * when the player is using items, sneaking, in water, on soul sand, or in cobwebs.
 *
 * <p>Each source of slowdown can be individually toggled via its own setting.
 */
public class NoSlowdown extends Module {

    private final BoolSetting cancelItemUse = register(new BoolSetting(
            "Item Use", "Prevent slowdown while eating, using a bow, etc.", true));

    private final BoolSetting cancelSneak = register(new BoolSetting(
            "Sneak", "Prevent slowdown while sneaking (sneak speed is still maintained)", true));

    private final BoolSetting cancelSoulSand = register(new BoolSetting(
            "Soul Sand", "Prevent slowdown on soul sand", true));

    private final BoolSetting cancelCobweb = register(new BoolSetting(
            "Cobweb", "Prevent slowdown inside cobwebs", true));

    private final BoolSetting cancelWater = register(new BoolSetting(
            "Water", "Prevent slowdown while swimming", true));

    public NoSlowdown() {
        super("NoSlowdown", "Cancels movement slowdowns from items, terrain, and liquids",
                Category.MOVEMENT);
    }

    /** Called by the mixin/event system when an item-use or shield/bow slowdown would apply. */
    @EventHandler
    public void onSlowdown(EventSlowdown event) {
        if (cancelItemUse.isEnabled()) {
            // Override slowdown factor to 1.0 (no slowdown)
            event.setSlowdownFactor(1.0f);
            event.cancel();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // ---- Soul sand ----
        if (cancelSoulSand.isEnabled()) {
            BlockPos below = mc.player.getBlockPos().down();
            if (mc.world.getBlockState(below).getBlock() == Blocks.SOUL_SAND
                    || mc.world.getBlockState(below).getBlock() == Blocks.SOUL_SOIL) {
                // Override velocity to maintain normal sprint speed
                if (mc.player.isSprinting()) {
                    var vel = mc.player.getVelocity();
                    mc.player.setVelocity(vel.x * 1.3, vel.y, vel.z * 1.3);
                }
            }
        }

        // ---- Cobweb ----
        if (cancelCobweb.isEnabled()) {
            BlockPos pos = mc.player.getBlockPos();
            if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                var vel = mc.player.getVelocity();
                // Cobweb severely reduces velocity; restore horizontal component
                float yaw = mc.player.getYaw();
                double moveX = -Math.sin(Math.toRadians(yaw)) * 0.1;
                double moveZ =  Math.cos(Math.toRadians(yaw)) * 0.1;
                mc.player.setVelocity(moveX, vel.y > -0.05 ? vel.y : -0.05, moveZ);
            }
        }

        // ---- Water ----
        if (cancelWater.isEnabled() && mc.player.isTouchingWater()) {
            var vel = mc.player.getVelocity();
            // Apply a water-speed boost to counteract resistance
            double speedBoost = 0.06;
            float yaw = mc.player.getYaw();
            boolean moving = mc.player.input.movementForward != 0
                          || mc.player.input.movementSideways != 0;
            if (moving) {
                double bx = -Math.sin(Math.toRadians(yaw)) * speedBoost;
                double bz =  Math.cos(Math.toRadians(yaw)) * speedBoost;
                mc.player.setVelocity(vel.x + bx, vel.y, vel.z + bz);
            }
        }

        // ---- Sneak slowdown ----
        if (cancelSneak.isEnabled() && mc.player.isSneaking()) {
            // Boost sneak speed back toward normal walk speed
            var vel = mc.player.getVelocity();
            float yaw = mc.player.getYaw();
            boolean moving = mc.player.input.movementForward != 0
                          || mc.player.input.movementSideways != 0;
            if (moving && mc.player.isOnGround()) {
                double factor = 0.06; // close to walk speed boost
                double bx = -Math.sin(Math.toRadians(yaw)) * factor;
                double bz =  Math.cos(Math.toRadians(yaw)) * factor;
                mc.player.setVelocity(vel.x + bx, vel.y, vel.z + bz);
            }
        }
    }
}
