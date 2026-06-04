package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SuperJump - greatly increases jump height by setting a high upward velocity
 * the moment the player presses the jump key while on the ground.
 *
 * <p>Unlike HighJump (which hooks EventJump), this module detects the jump key
 * press directly each tick and overrides Y velocity, giving immediate response
 * and compatibility with environments where EventJump may not fire.
 */
public class SuperJump extends Module {

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Upward velocity applied on jump (vanilla = 0.42)", 2.0, 0.5, 10.0));

    private boolean wasJumping = false;

    public SuperJump() {
        super("SuperJump", "Greatly increases jump height", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasJumping = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean jumpPressed = mc.options.jumpKey.isPressed();
        boolean onGround = mc.player.isOnGround();

        // Detect fresh jump key press while on ground
        if (jumpPressed && !wasJumping && onGround) {
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, height.get(), vel.z);
        }

        wasJumping = jumpPressed;
    }
}
