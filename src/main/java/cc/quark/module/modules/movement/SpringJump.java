package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

/**
 * SpringJump - on landing (Y velocity > -0.1 previous tick and now isOnGround),
 * immediately jumps again. Optionally applies extra upward velocity.
 */
public class SpringJump extends Module {

    private final BoolSetting sprintJump = register(new BoolSetting(
            "SprintJump", "Keep sprinting between jumps", true));
    private final DoubleSetting jumpBoost = register(new DoubleSetting(
            "JumpBoost", "Extra upward velocity added on each spring jump", 1.0, 0.5, 3.0));

    private double prevVelY = 0.0;
    private boolean wasInAir = false;

    public SpringJump() {
        super("SpringJump", "Automatically re-jumps on landing with extra upward velocity", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        prevVelY = 0.0;
        wasInAir = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        double velY = mc.player.getVelocity().y;
        boolean onGround = mc.player.isOnGround();

        // Detect landing: was in air (falling), now on ground
        if (wasInAir && onGround && prevVelY < -0.1) {
            if (sprintJump.isEnabled()) {
                mc.player.setSprinting(true);
            }
            mc.player.jump();
            // Apply extra upward velocity on top of jump
            Vec3d vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x, vel.y + jumpBoost.get(), vel.z);
        }

        prevVelY = velY;
        wasInAir = !onGround;
    }
}
