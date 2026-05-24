package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventJump;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * HighJump - multiplies the player's jump height by increasing the upward velocity
 * applied at the moment of the jump.
 *
 * <p>Vanilla jump velocity is {@code 0.42} blocks/tick.  We replace it with
 * {@code 0.42 * height} so a height of 2.0 doubles the jump height.
 */
public class HighJump extends Module {

    /** Vanilla initial jump Y-velocity. */
    private static final double VANILLA_JUMP_VELOCITY = 0.42;

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Jump height multiplier (1 = vanilla)", 2.0, 1.0, 5.0));

    public HighJump() {
        super("HighJump", "Multiplies jump height", Category.MOVEMENT);
    }

    @EventHandler
    public void onJump(EventJump event) {
        if (mc.player == null) return;

        // Apply amplified upward velocity immediately after the jump is initiated
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x, VANILLA_JUMP_VELOCITY * height.get(), vel.z);
    }
}
