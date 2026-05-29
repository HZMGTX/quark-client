package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;

/**
 * AirJump - grants extra mid-air jumps.  Tracks how many jumps remain and
 * resets the counter each time the player lands on the ground or starts
 * climbing.  Consumes one jump per key press and prevents double-counting
 * the same key-down event.
 */
public class AirJump extends Module {

    private final IntSetting extraJumps = register(new IntSetting(
            "Extra Jumps", "How many extra jumps are allowed before landing", 2, 1, 5));
    private final DoubleSetting jumpForce = register(new DoubleSetting(
            "Jump Force", "Upward velocity applied for each air jump", 0.42, 0.2, 1.0));
    private final BoolSetting resetFallDist = register(new BoolSetting(
            "Reset Fall", "Reset fall distance on each air jump", true));

    private int jumpsLeft = 0;
    /** True while the jump key is being held to prevent repeated fire. */
    private boolean jumpHeld = false;

    public AirJump() {
        super("AirJump", "Jump multiple times in the air", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        jumpsLeft = 0;
        jumpHeld  = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround() || mc.player.isClimbing();
        boolean jumpPressed = mc.options.jumpKey.isPressed();

        // Refill jump counter when landing
        if (onGround) {
            jumpsLeft = extraJumps.get();
            jumpHeld  = jumpPressed; // suppress immediate re-fire if key held on landing
            return;
        }

        // Air jump logic
        if (jumpPressed) {
            if (!jumpHeld && jumpsLeft > 0) {
                // Cancel existing downward momentum before launching upward
                double vx = mc.player.getVelocity().x;
                double vz = mc.player.getVelocity().z;
                mc.player.setVelocity(vx, jumpForce.get(), vz);
                if (resetFallDist.isEnabled()) mc.player.fallDistance = 0;
                jumpsLeft--;
                jumpHeld = true;
            }
        } else {
            jumpHeld = false;
        }
    }
}
